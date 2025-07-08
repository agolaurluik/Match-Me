package com.kood.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kood.backend.entity.LocationEntities.Location;
import com.kood.backend.entity.UserEntities.User;
import com.kood.backend.entity.UserEntities.UserLocation;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
    boolean existsByUserAndLocation(User user, Location location);

    boolean existsByUser(User user);

    boolean existsByLocation(Location location);

    Optional<UserLocation> findByUser(User user);
}
