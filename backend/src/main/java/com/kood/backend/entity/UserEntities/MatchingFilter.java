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

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
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