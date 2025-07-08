package com.kood.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kood.backend.entity.LocationEntities.NamedLocation;

public interface NamedLocationRepository extends JpaRepository<NamedLocation, Long> {
    @Query(value = """
            SELECT * FROM named_location
            WHERE ST_DWithin(
                geography(point),
                geography(ST_MakePoint(:longitude, :latitude)),
                :thresholdMeters
            )
            LIMIT 1
            """, nativeQuery = true)
    Optional<NamedLocation> findExistingLocation( // -----------
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("thresholdMeters") double thresholdMeters);
}
