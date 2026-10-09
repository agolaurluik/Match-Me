package com.kood.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.kood.backend.dto.LocationDTOs.LocationDTO;
import com.kood.backend.dto.LocationDTOs.UserLocationDTO;
import com.kood.backend.dto.UserDTOs.UserBioDTO;
import com.kood.backend.dto.UserDTOs.UserDTO;
import com.kood.backend.dto.UserDTOs.UserFullDTO;
import com.kood.backend.dto.UserDTOs.UserMeProfileDTO;
import com.kood.backend.dto.UserDTOs.UserProfileDTO;
import com.kood.backend.dto.UserDTOs.UserSmallDTO;
import com.kood.backend.entity.UserEntities.User;
import com.kood.backend.mapper.LocationMapper;
import com.kood.backend.mapper.UserMapper;
import com.kood.backend.security.UserDetailsImpl;
import com.kood.backend.service.ConnectionService;
import com.kood.backend.service.LocationService;
import com.kood.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

        private final ConnectionService connectionService;
        private final LocationService locationService;
        private final UserService userService;

        @Autowired
        private UserMapper userMapper;

        @GetMapping("/getLocation/{id}")
        public ResponseEntity<Map<String, Object>> getLocationById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                        @PathVariable @NonNull Long id) {
                LocationDTO retrievedLocation = locationService.getLocationById(id);
                Map<String, Object> response = new HashMap<>();
                if (retrievedLocation == null) {
                        response.put("message", "Location not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                response.put("location", retrievedLocation);
                response.put("message", "Successfully retrieved <location> with ID: " + id);
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @GetMapping("/get-username/{userId}")
        public ResponseEntity<Map<String, String>> getUsernameById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                        @PathVariable @NonNull Long userId) {
                User user = userService.getUserById(userId);
                Map<String, String> response = new HashMap<>();
                if (user == null) {
                        response.put("message", "User not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                response.put("username", user.getUsername());
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        public ResponseEntity<Map<String, Object>> getUserById(
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @PathVariable @NonNull Long id) {
                User retrievedUser = userService.getUserById(id);
                Map<String, Object> response = new HashMap<>();
                if (retrievedUser == null) {
                        response.put("message", "User not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                response.put("user", userMapper.toSmallDTO(retrievedUser));
                response.put("message", "Successfully retrieved <user> with ID: " + userDetails.getId());
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @GetMapping("/{id}/bio")
        public ResponseEntity<Map<String, Object>> getUserBioById(
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @PathVariable @NonNull Long id) {
                User retrievedUser = userService.getUserById(id);
                Map<String, Object> response = new HashMap<>();
                if (retrievedUser == null) {
                        response.put("message", "User not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                response.put("profile", userMapper.toBioDTO(retrievedUser));
                response.put("message", "Successfully retrieved bio for <user> with ID: " + userDetails.getId());
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @GetMapping("/{id}/profile")
        public ResponseEntity<Map<String, Object>> getUserProfileById(
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @PathVariable @NonNull Long id) {
                Long currentUserID = userDetails.getId();
                User user = userService.getUserById(id);

                if (user == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("message", "Not found"));
                }

                boolean allowed = connectionService.canView(currentUserID, id);

                if (!allowed) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("message", "Not found"));
                }

                UserProfileDTO retrievedUser = userMapper.toProfileDTO(user);
                Map<String, Object> response = new HashMap<>();
                if (retrievedUser == null) {
                        response.put("message", "Not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                response.put("profile", retrievedUser);
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @GetMapping("/me")
        public ResponseEntity<Map<String, Object>> getMe(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
                UserSmallDTO retrievedUser = userMapper
                                .toSmallDTO(userService.getUserById(Objects.requireNonNull(userDetails.getId())));
                Map<String, Object> response = new HashMap<>();
                if (retrievedUser == null) {
                        response.put("message", "User not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                response.put("bio", retrievedUser);
                response.put("message", "Successfully retrieved <me>");
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @GetMapping("/me/bio")
        public ResponseEntity<Map<String, Object>> getMeBio(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
                UserBioDTO retrievedUser = userMapper
                                .toBioDTO(userService.getUserById(Objects.requireNonNull(userDetails.getId())));
                Map<String, Object> response = new HashMap<>();
                if (retrievedUser == null) {
                        response.put("message", "<me> with the ID: " + userDetails.getId() + " not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                response.put("bio", retrievedUser);
                response.put("message", "Successfully retrieved <me> bio");
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @GetMapping("/me/profile")
        public ResponseEntity<Map<String, Object>> getMeProfile(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
                UserMeProfileDTO retrievedUser = userMapper
                                .toMeProfileDTO(userService.getUserById(Objects.requireNonNull(userDetails.getId())));
                Map<String, Object> response = new HashMap<>();

                if (retrievedUser == null) {
                        response.put("message", "<me> with the ID: " + userDetails.getId() + " not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                response.put("profile", retrievedUser);
                response.put("message", "Successfully retrieved <me> profile");
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @PostMapping("/me/profile/createUserLocation")
        public ResponseEntity<Map<String, Object>> createUserLocation(
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @RequestBody LocationDTO location) {
                Map<String, Object> response = new HashMap<>();
                if (location == null) {
                        response.put("message", "Creating userLocation failed - no location available");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                // In case of NamedLocation frontend discards name and only gives the data in
                // the same format as LocationDTO and that should behave identically
                LocationDTO createdLocation = LocationMapper.toDTO(locationService.createLocation(location));
                UserLocationDTO retrievedLocation = locationService.createUserLocation(createdLocation,
                                userDetails);

                response.put("userLocation", retrievedLocation);
                response.put("message", "UserLocation successfully created for user ID: " + userDetails.getId());
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @PutMapping("/me/profile/updateUserLocation")
        public ResponseEntity<Map<String, Object>> updateMeProfileLocation(
                        @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody LocationDTO locationData) {
                Map<String, Object> response = new HashMap<>();
                if (locationData == null) {
                        response.put("message", "Location binding failed - no location in input");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                UserLocationDTO newBinding = locationService.updateUserLocation(locationData, userDetails);

                response.put("locationId", newBinding.getLocationId());
                response.put("message", "Successfully updated <me> profile location");
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        @PatchMapping("/me/profile")
        public ResponseEntity<Map<String, Object>> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                        @RequestBody UserDTO userData) {
                UserFullDTO updatedUser = userService.updateProfile(userDetails, userData);
                Map<String, Object> response = new HashMap<>();
                response.put("fullProfile", updatedUser);
                response.put("message", "Successfully updated user data");
                System.err.println("User data patch successfully completed");
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

}
