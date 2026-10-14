package backend.users.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import backend.users.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Override
    @NonNull
    Optional<User> findById(@NonNull Long id);

    User findUserByEmail(String email); // This is used only by the algorithm --- needs to be verified

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query(value = """
            WITH sender_context AS (
                SELECT
                    v.id AS sender_id,
                    v.gender_id AS sender_gender_id,
                    vg.name AS sender_gender,
                    v.birth_date AS sender_birthdate,

                    mf.gender_preference,
                    mf.lowest_age,
                    mf.highest_age,
                    mf.radius,
                    mf.match_limit,

                    mf.interest_score,
                    mf.personality_score,
                    mf.nationality_score,
                    mf.purpose_score,

                    vn.name AS sender_nationality,
                    vp.name AS sender_purpose,
                    vl.point AS sender_point

                FROM users v

                JOIN matching_filter mf
                    ON mf.user_id = v.id

                LEFT JOIN gender vg
                    ON vg.id = v.gender_id

                LEFT JOIN nationality vn
                    ON vn.id = v.nationality_id

                LEFT JOIN purpose vp
                    ON vp.id = v.purpose_id

                LEFT JOIN user_location vul
                    ON vul.id = v.location_id

                LEFT JOIN location vl
                    ON vl.id = vul.location_id

                WHERE v.id = :senderId
            ),

            eligible_receivers AS (
                SELECT
                    u.id AS receiver_id,
                    vc.sender_id,

                    vc.interest_score,
                    vc.personality_score,
                    vc.nationality_score,
                    vc.purpose_score,

                    cmf.interest_score AS receiver_interest_score,
                    cmf.personality_score AS receiver_personality_score,
                    cmf.nationality_score AS receiver_nationality_score,
                    cmf.purpose_score AS receiver_purpose_score,

                    vc.sender_nationality,
                    vc.sender_purpose,

                    cn.name AS receiver_nationality,
                    cp.name AS receiver_purpose,

                    CASE
                        WHEN vc.sender_point IS NOT NULL
                            AND cl.point IS NOT NULL
                        THEN ST_Distance(
                            vc.sender_point::geography,
                            cl.point::geography
                        ) / 1000.0
                        ELSE NULL
                    END AS distance_km

                FROM users u

                CROSS JOIN sender_context vc

                JOIN matching_filter cmf
                    ON cmf.user_id = u.id

                LEFT JOIN gender g
                    ON g.id = u.gender_id

                LEFT JOIN nationality cn
                    ON cn.id = u.nationality_id

                LEFT JOIN purpose cp
                    ON cp.id = u.purpose_id

                LEFT JOIN user_location cul
                    ON cul.id = u.location_id

                LEFT JOIN location cl
                    ON cl.id = cul.location_id

                WHERE
                    -- Exclude the sender
                    u.id <> vc.sender_id

                    -- receiver satisfies sender's gender preference
                    AND CASE LOWER(vc.gender_preference)
                        WHEN 'lookingformen'
                            THEN LOWER(g.name) = 'male'
                        WHEN 'lookingforwomen'
                            THEN LOWER(g.name) = 'female'
                        WHEN 'lookingforother'
                            THEN LOWER(g.name) = 'other'
                        WHEN 'lookingforall'
                            THEN LOWER(g.name)
                                IN ('male', 'female', 'non-binary', 'unspecified')
                        ELSE FALSE
                    END

                    -- sender satisfies receiver's gender preference
                    AND CASE LOWER(cmf.gender_preference)
                        WHEN 'lookingformen'
                            THEN LOWER(vc.sender_gender) = 'male'
                        WHEN 'lookingforwomen'
                            THEN LOWER(vc.sender_gender) = 'female'
                        WHEN 'lookingforother'
                            THEN LOWER(vc.sender_gender) = 'other'
                        WHEN 'lookingforall'
                            THEN LOWER(vc.sender_gender)
                                IN ('male', 'female', 'non-binary', 'unspecified')
                        ELSE FALSE
                    END

                    -- receiver satisfies sender's age range
                    AND u.birth_date IS NOT NULL

                    AND DATE_PART(
                        'year',
                        AGE(CAST(:today AS date), u.birth_date)
                    ) >= COALESCE(vc.lowest_age, 0)

                    AND DATE_PART(
                        'year',
                        AGE(CAST(:today AS date), u.birth_date)
                    ) <= COALESCE(vc.highest_age, 200)

                    -- sender satisfies receiver's age range
                    AND vc.sender_birthdate IS NOT NULL

                    AND DATE_PART(
                        'year',
                        AGE(CAST(:today AS date), vc.sender_birthdate)
                    ) >= COALESCE(cmf.lowest_age, 0)

                    AND DATE_PART(
                        'year',
                        AGE(CAST(:today AS date), vc.sender_birthdate)
                    ) <= COALESCE(cmf.highest_age, 200)

                    -- receiver satisfies sender's radius
                    AND (
                        vc.radius IS NULL
                        OR (
                            vc.sender_point IS NOT NULL
                            AND cl.point IS NOT NULL
                            AND ST_DWithin(
                                vc.sender_point::geography,
                                cl.point::geography,
                                vc.radius * 1000.0
                            )
                        )
                    )

                    -- sender satisfies receiver's radius
                    AND (
                        cmf.radius IS NULL
                        OR (
                            vc.sender_point IS NOT NULL
                            AND cl.point IS NOT NULL
                            AND ST_DWithin(
                                cl.point::geography,
                                vc.sender_point::geography,
                                cmf.radius * 1000.0
                            )
                        )
                    )

                    -- Exclude existing connections in either direction
                    AND NOT EXISTS (
                        SELECT 1
                        FROM connection c
                        WHERE
                            (
                                c.sender_id = vc.sender_id
                                AND c.receiver_id = u.id
                            )
                            OR
                            (
                                c.receiver_id = vc.sender_id
                                AND c.sender_id = u.id
                            )
                    )
            ),

            scored_receivers AS (
                SELECT
                    ec.*,

                    interests.shared_interests,
                    personalities.shared_personalities,
                    interests.shared_interest_names,
                    personalities.shared_personality_names,

                    -- Total available attributes for each user
                    sender_interests.total AS sender_interest_count,
                    receiver_interests.total AS receiver_interest_count,

                    sender_personalities.total AS sender_personality_count,
                    receiver_personalities.total AS receiver_personality_count,

                    -- Calculate the reciprocal match score
                    (
                        interests.shared_interests
                            * (
                                COALESCE(ec.interest_score, 0)
                                + COALESCE(ec.receiver_interest_score, 0)
                            ) / 2.0

                        + personalities.shared_personalities
                            * (
                                COALESCE(ec.personality_score, 0)
                                + COALESCE(ec.receiver_personality_score, 0)
                            ) / 2.0

                        + CASE
                            WHEN ec.sender_nationality IS NOT NULL
                                AND ec.receiver_nationality IS NOT NULL
                                AND LOWER(ec.sender_nationality)
                                    = LOWER(ec.receiver_nationality)
                            THEN (
                                COALESCE(ec.nationality_score, 0)
                                + COALESCE(ec.receiver_nationality_score, 0)
                            ) / 2.0
                            ELSE 0
                        END

                        + CASE
                            WHEN ec.sender_purpose IS NOT NULL
                                AND ec.receiver_purpose IS NOT NULL
                                AND LOWER(ec.sender_purpose)
                                    = LOWER(ec.receiver_purpose)
                            THEN (
                                COALESCE(ec.purpose_score, 0)
                                + COALESCE(ec.receiver_purpose_score, 0)
                            ) / 2.0
                            ELSE 0
                        END
                    ) AS match_score,

                    -- Calculate the maximum possible score for this pair
                    (
                        LEAST(
                            sender_interests.total,
                            receiver_interests.total
                        ) * (
                            COALESCE(ec.interest_score, 0)
                            + COALESCE(ec.receiver_interest_score, 0)
                        ) / 2.0

                        + LEAST(
                            sender_personalities.total,
                            receiver_personalities.total
                        ) * (
                            COALESCE(ec.personality_score, 0)
                            + COALESCE(ec.receiver_personality_score, 0)
                        ) / 2.0

                        + CASE
                            WHEN ec.sender_nationality IS NOT NULL
                                AND ec.receiver_nationality IS NOT NULL
                            THEN (
                                COALESCE(ec.nationality_score, 0)
                                + COALESCE(ec.receiver_nationality_score, 0)
                            ) / 2.0
                            ELSE 0
                        END

                        + CASE
                            WHEN ec.sender_purpose IS NOT NULL
                                AND ec.receiver_purpose IS NOT NULL
                            THEN (
                                COALESCE(ec.purpose_score, 0)
                                + COALESCE(ec.receiver_purpose_score, 0)
                            ) / 2.0
                            ELSE 0
                        END
                    ) AS max_possible_score

                FROM eligible_receivers ec

                -- Count shared interests
                CROSS JOIN LATERAL (
                    SELECT
                        COUNT(DISTINCT i.id)::integer AS shared_interests,
                        COALESCE(
                            ARRAY_AGG(DISTINCT i.name ORDER BY i.name)
                                FILTER (WHERE i.name IS NOT NULL),
                            ARRAY[]::text[]
                        ) AS shared_interest_names
                    FROM user_interest vi
                    JOIN user_interest ci
                        ON ci.interest_id = vi.interest_id
                    JOIN interest i
                        ON i.id = vi.interest_id
                    WHERE vi.user_id = ec.sender_id
                    AND ci.user_id = ec.receiver_id
                ) interests

                -- Count shared personalities
                CROSS JOIN LATERAL (
                            SELECT
                COUNT(DISTINCT p.id)::integer AS shared_personalities,
                COALESCE(
                    ARRAY_AGG(DISTINCT p.name ORDER BY p.name)
                        FILTER (WHERE p.name IS NOT NULL),
                    ARRAY[]::text[]
                ) AS shared_personality_names
                    FROM user_personality vp
                    JOIN user_personality cp
                        ON cp.personality_id = vp.personality_id
                    JOIN personality p
                        ON p.id = vp.personality_id
                    WHERE vp.user_id = ec.sender_id
                    AND cp.user_id = ec.receiver_id
                ) personalities

                -- Count all sender interests
                CROSS JOIN LATERAL (
                    SELECT COUNT(DISTINCT interest_id)::integer AS total
                    FROM user_interest
                    WHERE user_id = ec.sender_id
                ) sender_interests

                -- Count all receiver interests
                CROSS JOIN LATERAL (
                    SELECT COUNT(DISTINCT interest_id)::integer AS total
                    FROM user_interest
                    WHERE user_id = ec.receiver_id
                ) receiver_interests

                -- Count all sender personality traits
                CROSS JOIN LATERAL (
                    SELECT COUNT(DISTINCT personality_id)::integer AS total
                    FROM user_personality
                    WHERE user_id = ec.sender_id
                ) sender_personalities

                -- Count all receiver personality traits
                CROSS JOIN LATERAL (
                    SELECT COUNT(DISTINCT personality_id)::integer AS total
                    FROM user_personality
                    WHERE user_id = ec.receiver_id
                ) receiver_personalities
            )

            -- Return the highest-scoring reciprocal receivers
            SELECT
                receiver_id,
                ROUND(
                    100.0 * match_score
                    / NULLIF(max_possible_score, 0)
                ) AS match_percentage,

                shared_interest_names,
                shared_personality_names,
                distance_km

            FROM scored_receivers

            WHERE match_score >= 1

            ORDER BY
                match_score DESC,
                receiver_id ASC

            LIMIT (
                SELECT match_limit
                FROM sender_context
            )
            """, nativeQuery = true)
    List<Object[]> findTopMatchingUsers(
            @Param("senderId") Long senderId,
            @Param("today") LocalDate today);

}