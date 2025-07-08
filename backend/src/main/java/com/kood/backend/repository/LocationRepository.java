package com.kood.backend.repository;

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

}
