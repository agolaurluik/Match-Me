package com.kood.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kood.backend.entity.LocationEntities.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {

        @Query(value = """
                        SELECT * FROM location
                        WHERE ST_DWithin(
                            geography(point),
                            geography(ST_MakePoint(:longitude, :latitude)),
                            :thresholdMeters
                        )
                        LIMIT 1
                        """, nativeQuery = true)

        Optional<Location> findExistingLocation(
                        @Param("longitude") double longitude,
                        @Param("latitude") double latitude,
                        @Param("thresholdMeters") double thresholdMeters);

        @Query(value = """
                        SELECT ST_Distance(
                            (SELECT geography(point) FROM location WHERE id = :locationId1),
                            (SELECT geography(point) FROM location WHERE id = :locationId2)
                        )
                        """, nativeQuery = true)
        Optional<Double> calculateDistanceBetween(
                        @Param("locationId1") Long locationId1,
                        @Param("locationId2") Long locationId2);

        @Query(value = """
                        SELECT u.id
                        FROM users u
                        JOIN user_location ul
                        ON ul.id = u.location_id
                        JOIN location l
                        ON l.id = ul.location_id
                        JOIN user_location viewer_ul
                        ON viewer_ul.id = :viewerLocationId
                        JOIN location viewer_l
                        ON viewer_l.id = viewer_ul.location_id
                        WHERE ST_DWithin(
                        l.point::geography,
                        viewer_l.point::geography,
                        :radiusMeters
                        )
                        """, nativeQuery = true)
        List<Long> findUserIdsWithinRadius(
                        @Param("viewerLocationId") Long viewerLocationId,
                        @Param("radiusMeters") double radiusMeters);

}
