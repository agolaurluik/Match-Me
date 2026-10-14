package backend.matching.algorithm;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import backend.connection.service.ConnectionService;
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
        // Repository query is in userRepository - the algorithm itself
        private final UserRepository userRepository;
        private final LocationRepository locationRepository;
        private final MatchingFilterRepository matchingFilterRepository;
        private final ConnectionService connectionService;
        private final UserService userService;
        private final UserMatchingFilterMapperImpl userMatchingFilterMapperImpl;
        private final MatchSigningUtility matchSigningUtility;

        public UserMatchDetailDTO getDetailedMatchInfo(
                        UserLocation currentUserLocation,
                        UserLocation receiverLocation,
                        User sender,
                        User receiver,
                        Integer interestScore,
                        Integer personalityScore,
                        Integer nationalityScore,
                        Integer purposeScore) {
                Long senderId = sender.getId();
                Long receiverId = receiver.getId();
                Set<String> senderInterests = sender.getInterests().stream()
                                .map(Objects::requireNonNull)
                                .map(interest -> interest.getName())
                                .collect(Collectors.toSet());
                Set<String> senderPersonalities = sender.getPersonalities().stream()
                                .map(Objects::requireNonNull)
                                .map(personality -> personality.getName())
                                .collect(Collectors.toSet());

                Set<String> receiverInterests = receiver.getInterests().stream()
                                .map(Objects::requireNonNull)
                                .map(interest -> interest.getName())
                                .collect(Collectors.toSet());
                Set<String> receiverPersonalities = receiver.getPersonalities().stream()
                                .map(Objects::requireNonNull)
                                .map(personality -> personality.getName())
                                .collect(Collectors.toSet());

                Set<String> sharedInterests = new HashSet<>(senderInterests);
                sharedInterests.retainAll(receiverInterests);

                Set<String> sharedPersonalities = new HashSet<>(senderPersonalities);
                sharedPersonalities.retainAll(receiverPersonalities);

                int sharedNationalityScore = 0;
                if (sender.getNationality() != null && receiver.getNationality() != null
                                && sender.getNationality().getName().equalsIgnoreCase(
                                                receiver.getNationality().getName())) {
                        sharedNationalityScore = nationalityScore;
                }

                int sharedPurposeScore = 0;
                if (sender.getPurpose() != null && receiver.getPurpose() != null
                                && sender.getPurpose().getName().equalsIgnoreCase(receiver.getPurpose().getName())) {
                        sharedPurposeScore = purposeScore;
                }

                Double distanceInMeters = locationRepository
                                .calculateDistanceBetween(currentUserLocation.getId(), receiverLocation.getId())
                                .orElse(null);

                double distanceA = distanceInMeters / 1000.0;
                double distance = (int) distanceA; // truncate

                int score = (sharedInterests.size() * interestScore) +
                                (sharedPersonalities.size() * personalityScore) +
                                sharedPurposeScore + sharedNationalityScore;
                int maxScore = (sender.getInterests().size() * interestScore)
                                + (sender.getPersonalities().size() * personalityScore) +
                                sharedPurposeScore + sharedNationalityScore;

                double matchPercentageDouble = ((double) score / (double) maxScore) * 100;

                int matchPercentage = (int) Math.round(matchPercentageDouble);

                String senderProfileImageName = userService.getUserProfileImageNameByUserId(senderId);
                String receiverProfileImageName = userService.getUserProfileImageNameByUserId(receiverId);
                Instant now = Instant.now();
                UserMatchDetailDTO dto = UserMatchDetailDTO.builder()
                                .senderId(senderId)
                                .receiverId(receiverId)
                                .connectionId(null)
                                .matchPercentage(matchPercentage)
                                .sharedInterestsSet(sharedInterests)
                                .sharedPersonalitiesSet(sharedPersonalities)
                                .distance(distance)
                                .senderProfileImageName(senderProfileImageName)
                                .receiverProfileImageName(receiverProfileImageName)
                                .createdAt(now)
                                .build();
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
                        Long senderId,
                        LocalDate today) {

                List<Object[]> rows = userRepository.findTopMatchingUsers(
                                senderId,
                                today);

                List<UserMatchDetailDTO> matches = new ArrayList<>();

                for (Object[] row : rows) {
                        Long receiverId = ((Number) row[0]).longValue();
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

                        String senderProfileImageName = userService.getUserProfileImageNameByUserId(senderId);
                        String receiverProfileImageName = userService.getUserProfileImageNameByUserId(receiverId);
                        Instant now = Instant.now();

                        UserMatchDetailDTO dto = UserMatchDetailDTO.builder()
                                        .senderId(senderId)
                                        .receiverId(receiverId)
                                        .connectionId(null)
                                        .matchPercentage(matchPercentage)
                                        .sharedInterestsSet(sharedInterests)
                                        .sharedPersonalitiesSet(sharedPersonalities)
                                        .distance(distance)
                                        .senderProfileImageName(senderProfileImageName)
                                        .receiverProfileImageName(receiverProfileImageName)
                                        .createdAt(now)
                                        .build();
                        dto.setSignature(matchSigningUtility.signMatchData(dto));
                        matches.add(dto);
                }
                matches.sort(Comparator.comparingDouble((UserMatchDetailDTO dto) -> dto.getMatchPercentage())
                                .reversed());
                return matches;
        }
}
