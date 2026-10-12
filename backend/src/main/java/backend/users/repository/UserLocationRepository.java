package backend.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.genericData.entity.Location;
import backend.users.entity.User;
import backend.users.entity.UserLocation;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
    boolean existsByUserAndLocation(User user, Location location);

    boolean existsByUser(User user);

    boolean existsByLocation(Location location);

    Optional<UserLocation> findByUser(User user);
}
