package backend.genericData.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import backend.genericData.entity.Purpose;

public interface PurposeRepository extends JpaRepository<Purpose, Long> {

    boolean existsByName(String purpose);

    Optional<Purpose> findByName(@Param("name") String name);
}
