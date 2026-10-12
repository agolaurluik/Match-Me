package backend.genericData.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import backend.genericData.entity.Personality;

public interface PersonalityRepository extends JpaRepository<Personality, Long> {
    Optional<Personality> findByName(@Param("name") String name);
}
