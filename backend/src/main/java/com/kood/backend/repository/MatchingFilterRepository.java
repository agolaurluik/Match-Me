package com.kood.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kood.backend.entity.UserEntities.MatchingFilter;

import java.util.Optional;

public interface MatchingFilterRepository extends JpaRepository<MatchingFilter, Long> {

    Optional<MatchingFilter> findByUserId(Long id);

}