package com.kood.backend.entity.UserEntities;

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
@Table(name = "matching_filter")
public class MatchingFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    private Integer match_limit;
    private String genderPreference;
    private Integer nationalityScore;
    private Integer interestScore;
    private Integer personalityScore;
    private Integer purposeScore;
    private Integer highestAge;
    private Integer lowestAge;
    private Integer radius;
}