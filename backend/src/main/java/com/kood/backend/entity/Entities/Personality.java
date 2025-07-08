package com.kood.backend.entity.Entities;

import java.util.HashSet;
import java.util.Set;

import com.kood.backend.entity.UserEntities.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Personality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "personalities")
    private Set<User> users = new HashSet<>();

}