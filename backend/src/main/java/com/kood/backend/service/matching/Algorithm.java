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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

                double matchPercentageDouble = ((double) score / (double) maxScore) * 100;

                int matchPercentageRounded = (int) Math.round(matchPercentageDouble);

                String profileImageName = userService.getUserProfileImageNameByUserId(candidate.getId());
                return new UserMatchDetailDTO(
                                candidate.getId(),
                                matchPercentageRounded,
                                sharedInterests,
                                sharedPersonalities,
                                distance,
                                profileImageName);
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

        public List<UserMatchDetailDTO> findTopMatchingUsers(
                        Long viewerId,
                        LocalDate today) {

                List<Object[]> rows = userRepository.findTopMatchingUsers(
                                viewerId,
                                today);

                List<UserMatchDetailDTO> matches = new ArrayList<>();

                for (Object[] row : rows) {
                        Long candidateId = ((Number) row[0]).longValue();
                        double matchPercentage = ((Number) row[1]).doubleValue();
                        Set<String> sharedInterests = row[2] == null
                                        ? new HashSet<>()
                                        : new HashSet<>(Arrays.asList((String[]) row[2]));

                        Set<String> sharedPersonalities = row[3] == null
                                        ? new HashSet<>()
                                        : new HashSet<>(Arrays.asList((String[]) row[3]));

                        Double distanceKm = row[4] == null
                                        ? null
                                        : ((Number) row[4]).doubleValue();
                        int distance = Math.toIntExact(Math.round(distanceKm));
                        String profileImageName = userService.getUserProfileImageNameByUserId(candidateId);
                        UserMatchDetailDTO dto = new UserMatchDetailDTO(
                                        candidateId,
                                        matchPercentage,
                                        sharedInterests,
                                        sharedPersonalities,
                                        distance,
                                        profileImageName);

                        matches.add(dto);
                }
                matches.sort(Comparator.comparingDouble((UserMatchDetailDTO dto) -> dto.getMatchPercentage())
                                .reversed());
                return matches;
        }
}
