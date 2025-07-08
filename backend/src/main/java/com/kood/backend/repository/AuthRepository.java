package com.kood.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kood.backend.entity.UserEntities.MatchingFilter;

@Repository
public interface AuthRepository extends JpaRepository<MatchingFilter, Long> {
    Optional<MatchingFilter> findByUserId(Long userId);
}
