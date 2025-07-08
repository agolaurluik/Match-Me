package com.kood.backend.entity.UserEntities;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kood.backend.entity.Entities.Gender;
import com.kood.backend.entity.Entities.Interest;
import com.kood.backend.entity.Entities.Nationality;
import com.kood.backend.entity.Entities.Personality;
import com.kood.backend.entity.Entities.Purpose;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "birthDate")

    private Instant birthDate;

    @Column(name = "lastSeen")
    private Instant lastSeen;

    @ManyToOne
    @JoinColumn(name = "gender_id")
    private Gender gender;

    @ManyToOne
    @JoinColumn(name = "nationality_id")
    private Nationality nationality;

    @Column(nullable = false)
    private String passwordHash;

    @Column(name = "profile_image_name")
    private String profileImageName;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "user_personality", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "personality_id"))
    private Set<Personality> personalities = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "location_id")
    private UserLocation location;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "user_interest", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "interest_id"))
    private Set<Interest> interests = new HashSet<>();

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private MatchingFilter matchingFilter;

    @ManyToOne
    @JoinColumn(name = "purpose_id")
    private Purpose purpose;

    @Lob
    @Column(name = "user_description", columnDefinition = "TEXT")
    private String userDescription;
}
