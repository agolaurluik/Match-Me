package backend.matching.algorithm;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import backend.genericData.repository.LocationRepository;
import backend.matching.dto.UserMatchDetailDTO;
import backend.matching.dto.UserMatchingFilterDTO;
import backend.matching.entity.MatchingFilter;
import backend.matching.mapper.UserMatchingFilterMapperImpl;
import backend.matching.repository.MatchingFilterRepository;
import backend.users.entity.User;
import backend.users.entity.UserLocation;
import backend.users.repository.UserRepository;
import backend.users.service.UserService;
import backend.utilities.HelperMethods.MatchSigningUtility;

import java.time.Instant;
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
        private final MatchSigningUtility matchSigningUtility;

        public UserMatchDetailDTO getDetailedMatchInfo(
                        UserLocation currentUserLocation,
                        UserLocation candidateLocation,
                        User viewer,
                        User candidate,
                        Integer interestScore,
                        Integer personalityScore,
                        Integer nationalityScore,
                        Integer purposeScore) {
                Long viewerId = viewer.getId();
                Long candidateId = candidate.getId();
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

                int matchPercentage = (int) Math.round(matchPercentageDouble);

                String viewerProfileImageName = userService.getUserProfileImageNameByUserId(viewerId);
                String candidateProfileImageName = userService.getUserProfileImageNameByUserId(candidateId);
                Instant now = Instant.now();
                UserMatchDetailDTO dto = new UserMatchDetailDTO(
                                viewerId,
                                candidateId,
                                matchPercentage,
                                sharedInterests,
                                sharedPersonalities,
                                distance,
                                viewerProfileImageName,
                                candidateProfileImageName,
                                now, null);
                dto.setSignature(matchSigningUtility.signMatchData(dto));
                return dto;
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

                        String viewerProfileImageName = userService.getUserProfileImageNameByUserId(viewerId);
                        String candidateProfileImageName = userService.getUserProfileImageNameByUserId(candidateId);
                        Instant now = Instant.now();
                        UserMatchDetailDTO dto = new UserMatchDetailDTO(
                                        viewerId,
                                        candidateId,
                                        matchPercentage,
                                        sharedInterests,
                                        sharedPersonalities,
                                        distance,
                                        viewerProfileImageName,
                                        candidateProfileImageName,
                                        now, null);
                        dto.setSignature(matchSigningUtility.signMatchData(dto));
                        matches.add(dto);
                }
                matches.sort(Comparator.comparingDouble((UserMatchDetailDTO dto) -> dto.getMatchPercentage())
                                .reversed());
                return matches;
        }
}
