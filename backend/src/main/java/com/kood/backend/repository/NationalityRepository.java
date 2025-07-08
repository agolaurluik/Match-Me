package com.kood.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.kood.backend.entity.Entities.Nationality;

public interface NationalityRepository extends JpaRepository<Nationality, Long> {
    Optional<Nationality> findByName(@Param("name") String name);
}
