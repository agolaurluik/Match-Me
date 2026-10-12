package backend.matching.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.matching.entity.MatchingFilter;

public interface MatchingFilterRepository extends JpaRepository<MatchingFilter, Long> {

    Optional<MatchingFilter> findByUserId(Long id);

}