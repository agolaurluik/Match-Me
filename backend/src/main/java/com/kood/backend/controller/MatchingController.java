package com.kood.backend.controller;

import com.kood.backend.dto.EntityDTOs.GenderDTO;
import com.kood.backend.dto.EntityDTOs.InterestDTO;
import com.kood.backend.dto.EntityDTOs.NationalityDTO;
import com.kood.backend.dto.EntityDTOs.PersonalityDTO;
import com.kood.backend.dto.EntityDTOs.PurposeDTO;
import com.kood.backend.dto.LocationDTOs.NamedLocationDTO;
import com.kood.backend.dto.algorithmDTOs.UserMatchDetailDTO;
import com.kood.backend.dto.algorithmDTOs.UserMatchingFilterDTO;
import com.kood.backend.entity.UserEntities.MatchingFilter;
import com.kood.backend.entity.UserEntities.User;
import com.kood.backend.entity.UserEntities.UserLocation;
import com.kood.backend.security.UserDetailsImpl;
import com.kood.backend.security.jwtconfig.JwtUtils;
import com.kood.backend.service.GenderService;
import com.kood.backend.service.InterestService;
import com.kood.backend.service.NamedLocationService;
import com.kood.backend.service.NationalityService;
import com.kood.backend.service.PersonalityService;
import com.kood.backend.service.PurposeService;
import com.kood.backend.service.UserService;
import com.kood.backend.service.matching.Algorithm;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class MatchingController {

        private final UserService userService;
        private final PersonalityService personalityService;
        private final InterestService interestService;
        private final PurposeService purposeService;
        private final NationalityService nationalityService;
        private final GenderService genderService;
        private final NamedLocationService namedLocationService;

        @Autowired
        JwtUtils jwtUtils;

        @Autowired
        Algorithm algorithm;

        @GetMapping("/recommendations")
        public ResponseEntity<Map<String, Object>> getRecommendations(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

                User currentUser = userService.getUserById(Objects.requireNonNull(userDetails.getId()));
                MatchingFilter filter = userService.getUserMatchingFilterByUserId(currentUser.getId());

                Map<String, Object> response = new HashMap<>();
                if (filter == null) {
                        response.put("error",
                                        "Matching filter not found for this user with id: " + userDetails.getId());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(response);
                }
                LocalDate today = LocalDate.now(ZoneId.of("Europe/Tallinn"));

                List<UserMatchDetailDTO> topIds = algorithm.findTopMatchingUsers(currentUser.getId(), today);

                response.put("recommendations", topIds);
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @GetMapping("/getMatchInfoByID/{id}")
        public ResponseEntity<Map<String, Object>> getMatchInfoByCandidateId(
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @PathVariable @NonNull Long id) {

                User currentUser = userService.getUserById(Objects.requireNonNull(userDetails.getId()));
                User candidate = userService.getUserById(id);
                MatchingFilter filter = userService.getUserMatchingFilterByUserId(currentUser.getId());
                Map<String, Object> response = new HashMap<>();
                if (filter == null) {
                        response.put("error",
                                        "Matching filter not found for this user with id: " + userDetails.getId());
                        return ResponseEntity.status(HttpStatus.OK)
                                        .body(response);
                }

                UserLocation currentUserLocation = currentUser.getLocation();
                UserLocation candidateLocation = candidate.getLocation();

                UserMatchDetailDTO matchDetail = algorithm.getDetailedMatchInfo(
                                currentUserLocation,
                                candidateLocation,
                                currentUser,
                                candidate,
                                filter.getInterestScore(),
                                filter.getPersonalityScore(),
                                filter.getNationalityScore(),
                                filter.getPurposeScore());

                response.put("matchInfo", matchDetail);
                response.put("message", "Successfully retrieved match info for user id: " + userDetails.getId()
                                + " and candidate id: " + candidate.getId());
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @GetMapping("/filter")
        public ResponseEntity<Map<String, Object>> getUserMatchingFilter(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
                UserMatchingFilterDTO retrievedFilter = userService.getUserMatchingFilter(userDetails);
                Map<String, Object> response = new HashMap<>();
                response.put("matchingFilter", retrievedFilter);
                response.put("message", "Successfully retrieved matchingFilter for user id: " + userDetails.getId());
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @PatchMapping("/updateMatchingFilter")
        public ResponseEntity<Map<String, Object>> updateDataForFilter(
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @RequestBody UserMatchingFilterDTO points) {

                User currentUser = userService.getUserById(Objects.requireNonNull(userDetails.getId()));
                UserMatchingFilterDTO updated = algorithm.updateMatchingFilter(currentUser, points);

                Map<String, Object> response = new HashMap<>();
                response.put("updatedMatchingFilter", updated);
                response.put("message", "Successfully updated matchingFilter for user with id: " + currentUser.getId());
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @GetMapping("/getAllPersonalities")
        public ResponseEntity<Map<String, Object>> getAllPersonalities(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
                Set<PersonalityDTO> retrievedPersonalities = personalityService.getAllPersonalities();
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Successfully retrieved all personalities");
                response.put("personalities", retrievedPersonalities);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/getAllInterests")
        public ResponseEntity<Map<String, Object>> getAllInterests(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
                Set<InterestDTO> retrievedInterests = interestService.getAllInterests();
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Successfully retrieved all interests");
                response.put("interests", retrievedInterests);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/getAllPurposes")
        public ResponseEntity<Map<String, Object>> getAllPurposes(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
                Set<PurposeDTO> retrievedPurposes = purposeService.getAllPurposes();
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Successfully retrieved all purposes");
                response.put("purposes", retrievedPurposes);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/getAllNationalities")
        public ResponseEntity<Map<String, Object>> getAllNationalities(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
                Set<NationalityDTO> retrievedNationalities = nationalityService.getAllNationalities();
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Successfully retrieved all nationalities");
                response.put("nationalities", retrievedNationalities);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/getAllGenders")
        public ResponseEntity<Map<String, Object>> getAllGenders(@AuthenticationPrincipal UserDetailsImpl userDetails) {
                Set<GenderDTO> retrievedGenders = genderService.getAllGenders();
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Successfully retrieved all genders");
                response.put("genders", retrievedGenders);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/getAllNamedLocations")
        public ResponseEntity<Map<String, Object>> getAllNamedLocations(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
                Set<NamedLocationDTO> retrievedNamedLocations = namedLocationService.getAllNamedLocations();
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Successfully retrieved all namedLocations");
                response.put("namedLocations", retrievedNamedLocations);
                return ResponseEntity.ok(response);
        }

}