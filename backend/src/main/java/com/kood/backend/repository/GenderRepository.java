package com.kood.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.kood.backend.entity.Entities.Gender;

public interface GenderRepository extends JpaRepository<Gender, Long> {
    Optional<Gender> findByName(@Param("name") String name);
}
