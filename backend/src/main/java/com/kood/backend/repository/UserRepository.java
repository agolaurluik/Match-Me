package com.kood.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import com.kood.backend.entity.UserEntities.User;

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
            WITH viewer_context AS (
                SELECT
                    v.id AS viewer_id,
                    v.gender_id AS viewer_gender_id,
                    vg.name AS viewer_gender,
                    v.birth_date AS viewer_birthdate,

                    mf.gender_preference,
                    mf.lowest_age,
                    mf.highest_age,
                    mf.radius,
                    mf.match_limit,

                    mf.interest_score,
                    mf.personality_score,
                    mf.nationality_score,
                    mf.purpose_score,

                    vn.name AS viewer_nationality,
                    vp.name AS viewer_purpose,
                    vl.point AS viewer_point

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

                WHERE v.id = :viewerId
            ),

            eligible_candidates AS (
                SELECT
                    u.id AS candidate_id,
                    vc.viewer_id,

                    vc.interest_score,
                    vc.personality_score,
                    vc.nationality_score,
                    vc.purpose_score,

                    cmf.interest_score AS candidate_interest_score,
                    cmf.personality_score AS candidate_personality_score,
                    cmf.nationality_score AS candidate_nationality_score,
                    cmf.purpose_score AS candidate_purpose_score,

                    vc.viewer_nationality,
                    vc.viewer_purpose,

                    cn.name AS candidate_nationality,
                    cp.name AS candidate_purpose,

                    CASE
                        WHEN vc.viewer_point IS NOT NULL
                            AND cl.point IS NOT NULL
                        THEN ST_Distance(
                            vc.viewer_point::geography,
                            cl.point::geography
                        ) / 1000.0
                        ELSE NULL
                    END AS distance_km

                FROM users u

                CROSS JOIN viewer_context vc

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
                    -- Exclude the viewer
                    u.id <> vc.viewer_id

                    -- Candidate satisfies viewer's gender preference
                    AND CASE LOWER(vc.gender_preference)
                        WHEN 'lookingformen'
                            THEN LOWER(g.name) = 'male'
                        WHEN 'lookingforwomen'
                            THEN LOWER(g.name) = 'female'
                        WHEN 'lookingforother'
                            THEN LOWER(g.name) = 'other'
                        WHEN 'lookingforall'
                            THEN LOWER(g.name)
                                IN ('male', 'female', 'other')
                        ELSE FALSE
                    END

                    -- Viewer satisfies candidate's gender preference
                    AND CASE LOWER(cmf.gender_preference)
                        WHEN 'lookingformen'
                            THEN LOWER(vc.viewer_gender) = 'male'
                        WHEN 'lookingforwomen'
                            THEN LOWER(vc.viewer_gender) = 'female'
                        WHEN 'lookingforother'
                            THEN LOWER(vc.viewer_gender) = 'other'
                        WHEN 'lookingforall'
                            THEN LOWER(vc.viewer_gender)
                                IN ('male', 'female', 'other')
                        ELSE FALSE
                    END

                    -- Candidate satisfies viewer's age range
                    AND u.birth_date IS NOT NULL

                    AND DATE_PART(
                        'year',
                        AGE(CAST(:today AS date), u.birth_date)
                    ) >= COALESCE(vc.lowest_age, 0)

                    AND DATE_PART(
                        'year',
                        AGE(CAST(:today AS date), u.birth_date)
                    ) <= COALESCE(vc.highest_age, 200)

                    -- Viewer satisfies candidate's age range
                    AND vc.viewer_birthdate IS NOT NULL

                    AND DATE_PART(
                        'year',
                        AGE(CAST(:today AS date), vc.viewer_birthdate)
                    ) >= COALESCE(cmf.lowest_age, 0)

                    AND DATE_PART(
                        'year',
                        AGE(CAST(:today AS date), vc.viewer_birthdate)
                    ) <= COALESCE(cmf.highest_age, 200)

                    -- Candidate satisfies viewer's radius
                    AND (
                        vc.radius IS NULL
                        OR (
                            vc.viewer_point IS NOT NULL
                            AND cl.point IS NOT NULL
                            AND ST_DWithin(
                                vc.viewer_point::geography,
                                cl.point::geography,
                                vc.radius * 1000.0
                            )
                        )
                    )

                    -- Viewer satisfies candidate's radius
                    AND (
                        cmf.radius IS NULL
                        OR (
                            vc.viewer_point IS NOT NULL
                            AND cl.point IS NOT NULL
                            AND ST_DWithin(
                                cl.point::geography,
                                vc.viewer_point::geography,
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
                                c.sender_id = vc.viewer_id
                                AND c.receiver_id = u.id
                            )
                            OR
                            (
                                c.receiver_id = vc.viewer_id
                                AND c.sender_id = u.id
                            )
                    )
            ),

            scored_candidates AS (
                SELECT
                    ec.*,

                    interests.shared_interests,
                    personalities.shared_personalities,
                    interests.shared_interest_names,
                    personalities.shared_personality_names,

                    -- Total available attributes for each user
                    viewer_interests.total AS viewer_interest_count,
                    candidate_interests.total AS candidate_interest_count,

                    viewer_personalities.total AS viewer_personality_count,
                    candidate_personalities.total AS candidate_personality_count,

                    -- Calculate the reciprocal match score
                    (
                        interests.shared_interests
                            * (
                                COALESCE(ec.interest_score, 0)
                                + COALESCE(ec.candidate_interest_score, 0)
                            ) / 2.0

                        + personalities.shared_personalities
                            * (
                                COALESCE(ec.personality_score, 0)
                                + COALESCE(ec.candidate_personality_score, 0)
                            ) / 2.0

                        + CASE
                            WHEN ec.viewer_nationality IS NOT NULL
                                AND ec.candidate_nationality IS NOT NULL
                                AND LOWER(ec.viewer_nationality)
                                    = LOWER(ec.candidate_nationality)
                            THEN (
                                COALESCE(ec.nationality_score, 0)
                                + COALESCE(ec.candidate_nationality_score, 0)
                            ) / 2.0
                            ELSE 0
                        END

                        + CASE
                            WHEN ec.viewer_purpose IS NOT NULL
                                AND ec.candidate_purpose IS NOT NULL
                                AND LOWER(ec.viewer_purpose)
                                    = LOWER(ec.candidate_purpose)
                            THEN (
                                COALESCE(ec.purpose_score, 0)
                                + COALESCE(ec.candidate_purpose_score, 0)
                            ) / 2.0
                            ELSE 0
                        END
                    ) AS match_score,

                    -- Calculate the maximum possible score for this pair
                    (
                        LEAST(
                            viewer_interests.total,
                            candidate_interests.total
                        ) * (
                            COALESCE(ec.interest_score, 0)
                            + COALESCE(ec.candidate_interest_score, 0)
                        ) / 2.0

                        + LEAST(
                            viewer_personalities.total,
                            candidate_personalities.total
                        ) * (
                            COALESCE(ec.personality_score, 0)
                            + COALESCE(ec.candidate_personality_score, 0)
                        ) / 2.0

                        + CASE
                            WHEN ec.viewer_nationality IS NOT NULL
                                AND ec.candidate_nationality IS NOT NULL
                            THEN (
                                COALESCE(ec.nationality_score, 0)
                                + COALESCE(ec.candidate_nationality_score, 0)
                            ) / 2.0
                            ELSE 0
                        END

                        + CASE
                            WHEN ec.viewer_purpose IS NOT NULL
                                AND ec.candidate_purpose IS NOT NULL
                            THEN (
                                COALESCE(ec.purpose_score, 0)
                                + COALESCE(ec.candidate_purpose_score, 0)
                            ) / 2.0
                            ELSE 0
                        END
                    ) AS max_possible_score

                FROM eligible_candidates ec

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
                    WHERE vi.user_id = ec.viewer_id
                    AND ci.user_id = ec.candidate_id
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
                    WHERE vp.user_id = ec.viewer_id
                    AND cp.user_id = ec.candidate_id
                ) personalities

                -- Count all viewer interests
                CROSS JOIN LATERAL (
                    SELECT COUNT(DISTINCT interest_id)::integer AS total
                    FROM user_interest
                    WHERE user_id = ec.viewer_id
                ) viewer_interests

                -- Count all candidate interests
                CROSS JOIN LATERAL (
                    SELECT COUNT(DISTINCT interest_id)::integer AS total
                    FROM user_interest
                    WHERE user_id = ec.candidate_id
                ) candidate_interests

                -- Count all viewer personality traits
                CROSS JOIN LATERAL (
                    SELECT COUNT(DISTINCT personality_id)::integer AS total
                    FROM user_personality
                    WHERE user_id = ec.viewer_id
                ) viewer_personalities

                -- Count all candidate personality traits
                CROSS JOIN LATERAL (
                    SELECT COUNT(DISTINCT personality_id)::integer AS total
                    FROM user_personality
                    WHERE user_id = ec.candidate_id
                ) candidate_personalities
            )

            -- Return the highest-scoring reciprocal candidates
            SELECT
                candidate_id,
                ROUND(
                    100.0 * match_score
                    / NULLIF(max_possible_score, 0)
                ) AS match_percentage,

                shared_interest_names,
                shared_personality_names,
                distance_km

            FROM scored_candidates

            WHERE match_score >= 3

            ORDER BY
                match_score DESC,
                candidate_id ASC

            LIMIT (
                SELECT match_limit
                FROM viewer_context
            )
            """, nativeQuery = true)
    List<Object[]> findTopMatchingUsers(
            @Param("viewerId") Long viewerId,
            @Param("today") LocalDate today);

}