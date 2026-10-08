package com.kood.backend.service.matching;

import com.kood.backend.entity.UserEntities.MatchingFilter;
import com.kood.backend.entity.UserEntities.User;
import com.kood.backend.entity.UserEntities.UserLocation;
import com.kood.backend.mapper.UserMatchingFilterMapperImpl;
import com.kood.backend.dto.algorithmDTOs.UserMatchDetailDTO;
import com.kood.backend.dto.algorithmDTOs.UserMatchingFilterDTO;
import com.kood.backend.repository.UserRepository;
import com.kood.backend.service.UserService;

import lombok.RequiredArgsConstructor;

import com.kood.backend.repository.LocationRepository;
import com.kood.backend.repository.MatchingFilterRepository;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Algorithm {

        private final UserRepository userRepository;
        private final LocationRepository locationRepository;
        private final MatchingFilterRepository matchingFilterRepository;
        private final UserService userService;
        private final UserMatchingFilterMapperImpl userMatchingFilterMapperImpl;

        private boolean matchesGenderPreference(User candidate, String genderPreference) {
                if (candidate.getGender() == null) {
                        System.err.println("Skipping candidate with ID " + candidate.getId() + ": gender is null.");
                        return false; // Skip the user with null gender
                }

                return switch (genderPreference.toLowerCase()) {
                        case "lookingformen" -> "male".equalsIgnoreCase(candidate.getGender().getName());
                        case "lookingforwomen" -> "female".equalsIgnoreCase(candidate.getGender().getName());
                        case "lookingforother" -> "other".equalsIgnoreCase(candidate.getGender().getName());
                        case "lookingforall" -> true;
                        default -> false; // Invalid preference, exclude by default
                };
        }

        private boolean ageMatches(Instant birthDate, Integer highestAge, Integer lowestAge) {
                if (birthDate == null)
                        return false;
                LocalDate birthDateLocal = birthDate.atZone(ZoneId.systemDefault()).toLocalDate();
                int age = Period.between(birthDateLocal, LocalDate.now()).getYears();

                if (lowestAge != null && age < lowestAge)
                        return false;
                if (highestAge != null && age > highestAge)
                        return false;

                return true;
        }

        private boolean radiusMatches(User candidate, User viewer, Integer radius) {
                if (radius == null)
                        return true; // No filter applied

                UserLocation viewerLocation = viewer.getLocation();
                UserLocation candidateLocation = candidate.getLocation();

                if (viewerLocation == null || candidateLocation == null)
                        return false;

                Double distanceInMeters = locationRepository
                                .calculateDistanceBetween(viewerLocation.getId(), candidateLocation.getId())
                                .orElse(null);

                if (distanceInMeters == null)
                        return false;

                return (distanceInMeters / 1000.0) <= radius;
        }

        public List<Long> getTopMatchingUsersIDs(
                        User viewer,
                        Integer match_limit,
                        String genderPreference,
                        Integer nationalityScore,
                        Integer interestScore,
                        Integer personalityScore,
                        Integer purposeScore,
                        Integer highestAge,
                        Integer lowestAge,
                        Integer radius,
                        List<Long> connectedUserIds) {

                return userRepository.findAll().stream()
                                .filter(candidate -> !candidate.getId().equals(viewer.getId()))
                                .filter(candidate -> matchesGenderPreference(candidate, genderPreference))
                                .filter(candidate -> ageMatches(candidate.getBirthDate(), highestAge, lowestAge))
                                .filter(candidate -> radiusMatches(candidate, viewer, radius))
                                .filter(candidate -> !connectedUserIds.contains(candidate.getId()))

                                .map(candidate -> {
                                        Set<String> viewerInterests = viewer.getInterests().stream()
                                                        .map(Objects::requireNonNull)
                                                        .map(interest -> interest.getName())
                                                        .collect(Collectors.toSet());
                                        Set<String> viewerPersonalities = viewer.getPersonalities().stream()
                                                        .map(Objects::requireNonNull)
                                                        .map(personality -> personality.getName())
                                                        .collect(Collectors.toSet());

                                        Set<String> candidateInterests = candidate.getInterests().stream()
                                                        .map(Objects::requireNonNull)
                                                        .map(interest -> interest.getName())
                                                        .collect(Collectors.toSet());
                                        Set<String> candidatePersonalities = candidate.getPersonalities().stream()
                                                        .map(Objects::requireNonNull)
                                                        .map(personality -> personality.getName())
                                                        .collect(Collectors.toSet());

                                        Set<String> sharedInterests = new HashSet<>(viewerInterests);
                                        sharedInterests.retainAll(candidateInterests);

                                        Set<String> sharedPersonalities = new HashSet<>(viewerPersonalities);
                                        sharedPersonalities.retainAll(candidatePersonalities);

                                        // ----------------- purpose calculations:

                                        int sharedPurposeScore = 0;
                                        if (viewer.getPurpose() != null && candidate.getPurpose() != null
                                                        && viewer.getPurpose().getName().equalsIgnoreCase(
                                                                        candidate.getPurpose().getName())) {
                                                sharedPurposeScore = purposeScore;
                                        }

                                        // -------------- nationality calculations:

                                        int sharedNationalityScore = 0;
                                        if (viewer.getNationality() != null && candidate.getNationality() != null
                                                        && viewer.getNationality().getName().equalsIgnoreCase(
                                                                        candidate.getNationality().getName())) {
                                                sharedNationalityScore = nationalityScore;
                                        }

                                        // ---------------------------------------

                                        int score = (sharedInterests.size() * interestScore)
                                                        + (sharedPersonalities.size() * personalityScore)
                                                        + sharedPurposeScore + sharedNationalityScore;

                                        return new AbstractMap.SimpleEntry<>(candidate.getId(), score);
                                })
                                .filter(entry -> entry.getValue() >= 3)
                                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                                .limit(match_limit)
                                .map(entry -> entry.getKey())// check this for failure
                                .collect(Collectors.toList());
        }

        public UserMatchDetailDTO getDetailedMatchInfo(
                        UserLocation currentUserLocation,
                        UserLocation candidateLocation,
                        User viewer,
                        User candidate,
                        Integer interestScore,
                        Integer personalityScore,
                        Integer nationalityScore,
                        Integer purposeScore) {
                Set<String> viewerInterests = viewer.getInterests().stream()
                                .map(Objects::requireNonNull)
                                .map(interest -> interest.getName())
                                .collect(Collectors.toSet());
                Set<String> viewerPersonalities = viewer.getPersonalities().stream()
                                .map(Objects::requireNonNull)
                                .map(personality -> personality.getName())
                                .collect(Collectors.toSet());

                Set<String> candidateInterests = candidate.getInterests().stream()
                                .map(Objects::requireNonNull)
                                .map(interest -> interest.getName())
                                .collect(Collectors.toSet());
                Set<String> candidatePersonalities = candidate.getPersonalities().stream()
                                .map(Objects::requireNonNull)
                                .map(personality -> personality.getName())
                                .collect(Collectors.toSet());

                Set<String> sharedInterests = new HashSet<>(viewerInterests);
                sharedInterests.retainAll(candidateInterests);

                Set<String> sharedPersonalities = new HashSet<>(viewerPersonalities);
                sharedPersonalities.retainAll(candidatePersonalities);

                int sharedNationalityScore = 0;
                if (viewer.getNationality() != null && candidate.getNationality() != null
                                && viewer.getNationality().getName().equalsIgnoreCase(
                                                candidate.getNationality().getName())) {
                        sharedNationalityScore = nationalityScore;
                }

                int sharedPurposeScore = 0;
                if (viewer.getPurpose() != null && candidate.getPurpose() != null
                                && viewer.getPurpose().getName().equalsIgnoreCase(candidate.getPurpose().getName())) {
                        sharedPurposeScore = purposeScore;
                }

                Double distanceInMeters = locationRepository
                                .calculateDistanceBetween(currentUserLocation.getId(), candidateLocation.getId())
                                .orElse(null);

                double distanceA = distanceInMeters / 1000.0;
                double distance = (int) distanceA; // truncate

                int score = (sharedInterests.size() * interestScore) +
                                (sharedPersonalities.size() * personalityScore) +
                                sharedPurposeScore + sharedNationalityScore;
                int maxScore = (viewer.getInterests().size() * interestScore)
                                + (viewer.getPersonalities().size() * personalityScore) +
                                sharedPurposeScore + sharedNationalityScore;

                double matchPercentageDouble = score / maxScore;

                int matchPercentageRounded = (int) Math.round(matchPercentageDouble);

                return new UserMatchDetailDTO(
                                candidate.getId(),
                                distance,
                                matchPercentageRounded,
                                sharedInterests,
                                sharedPersonalities);
        }

        public UserMatchingFilterDTO updateMatchingFilter(User user, UserMatchingFilterDTO points) {
                MatchingFilter filter = userService.getUserMatchingFilterByUserId(user.getId());

                if (filter == null) {
                        filter = new MatchingFilter();
                        filter.setUserId(user.getId());
                        matchingFilterRepository.save(filter);
                }

                // Time to overwrite the old filter values
                filter.setGenderPreference(points.getGenderPreference());
                filter.setNationalityScore(points.getNationalityScore());
                filter.setInterestScore(points.getInterestScore());
                filter.setPersonalityScore(points.getPersonalityScore());
                filter.setPurposeScore(points.getPurposeScore());
                filter.setHighestAge(points.getHighestAge());
                filter.setLowestAge(points.getLowestAge());
                filter.setRadius(points.getRadius());

                userRepository.save(user);
                return userMatchingFilterMapperImpl.toDTO(filter);
        }

        public List<UserMatchDetailDTO> getTopMatchingUserDetails(
                        User viewer,
                        Integer match_limit,
                        String genderPreference,
                        Integer nationalityScore,
                        Integer interestScore,
                        Integer personalityScore,
                        Integer purposeScore,
                        Integer highestAge,
                        Integer lowestAge,
                        Integer radius,
                        List<Long> connectedUserIds) {

                return userRepository.findAll().stream()
                                .filter(candidate -> !candidate.getId().equals(viewer.getId()))
                                .filter(candidate -> matchesGenderPreference(candidate, genderPreference))
                                .filter(candidate -> ageMatches(candidate.getBirthDate(), highestAge, lowestAge))
                                .filter(candidate -> radiusMatches(candidate, viewer, radius))
                                .filter(candidate -> !connectedUserIds.contains(candidate.getId()))
                                .map(candidate -> {

                                        Set<String> viewerInterests = viewer.getInterests().stream()
                                                        .map(Objects::requireNonNull)
                                                        .map(interest -> interest.getName())
                                                        .collect(Collectors.toSet());
                                        Set<String> viewerPersonalities = viewer.getPersonalities().stream()
                                                        .map(Objects::requireNonNull)
                                                        .map(personality -> personality.getName())
                                                        .collect(Collectors.toSet());

                                        Set<String> candidateInterests = candidate.getInterests().stream()
                                                        .map(Objects::requireNonNull)
                                                        .map(interest -> interest.getName())
                                                        .collect(Collectors.toSet());
                                        Set<String> candidatePersonalities = candidate.getPersonalities().stream()
                                                        .map(Objects::requireNonNull)
                                                        .map(personality -> personality.getName())
                                                        .collect(Collectors.toSet());

                                        Set<String> sharedInterests = new HashSet<>(viewerInterests);
                                        sharedInterests.retainAll(candidateInterests);

                                        Set<String> sharedPersonalities = new HashSet<>(viewerPersonalities);
                                        sharedPersonalities.retainAll(candidatePersonalities);

                                        int sharedPurposeScore = 0;
                                        if (viewer.getPurpose() != null && candidate.getPurpose() != null &&
                                                        viewer.getPurpose().getName().equalsIgnoreCase(
                                                                        candidate.getPurpose().getName())) {
                                                sharedPurposeScore = purposeScore;
                                        }

                                        int sharedNationalityScore = 0;
                                        if (viewer.getNationality() != null && candidate.getNationality() != null &&
                                                        viewer.getNationality().getName().equalsIgnoreCase(
                                                                        candidate.getNationality().getName())) {
                                                sharedNationalityScore = nationalityScore;
                                        }

                                        int score = (sharedInterests.size() * interestScore)
                                                        + (sharedPersonalities.size() * personalityScore)
                                                        + sharedPurposeScore + sharedNationalityScore;

                                        int maxScore = (viewer.getInterests().size() * interestScore)
                                                        + (viewer.getPersonalities().size() * personalityScore) +
                                                        sharedPurposeScore + sharedNationalityScore;

                                        double matchPercentageDouble = score / maxScore;

                                        int matchPercentageRounded = (int) Math.round(matchPercentageDouble);

                                        double distanceA = locationRepository
                                                        .calculateDistanceBetween(viewer.getLocation().getId(),
                                                                        candidate.getLocation().getId())
                                                        .orElse(0.0) / 1000.0;

                                        double distance = (int) distanceA;

                                        return new UserMatchDetailDTO(
                                                        candidate.getId(),
                                                        distance,
                                                        matchPercentageRounded,
                                                        sharedInterests,
                                                        sharedPersonalities);
                                })
                                .sorted((a, b) -> Integer.compare(b.getMatchPercentageRounded(),
                                                a.getMatchPercentageRounded()))
                                .limit(match_limit)
                                .collect(Collectors.toList());
        }
}
