package com.kood.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kood.backend.dto.EntityDTOs.GenderDTO;
import com.kood.backend.dto.EntityDTOs.InterestDTO;
import com.kood.backend.dto.EntityDTOs.NationalityDTO;
import com.kood.backend.dto.EntityDTOs.PersonalityDTO;
import com.kood.backend.dto.EntityDTOs.PurposeDTO;
import com.kood.backend.dto.LocationDTOs.LocationDTO;
import com.kood.backend.dto.LocationDTOs.NamedLocationDTO;
import com.kood.backend.entity.UserEntities.User;
import com.kood.backend.mapper.LocationMapper;
import com.kood.backend.security.UserDetailsImpl;
import com.kood.backend.service.*;

//////////////////////////////////////// THIS IS FOR TESTING ONLY, REMOVE WHEN BACKEND IS FUNCTIONAL
@RestController
@RequestMapping("/api/test")
public class TESTEntityController {

        private final InterestService interestService;
        private final PersonalityService personalityService;
        private final PurposeService purposeService;
        private final GenderService genderService;
        private final NationalityService nationalityService;
        private final UserService userService;
        private final LocationService locationService;
        private final NamedLocationService namedLocationService;

        public TESTEntityController(UserService userService, ChatMessageService chatMessageService,
                        PurposeService purposeService, PersonalityService personalityService,
                        InterestService interestService, GenderService genderService,
                        NationalityService nationalityService,
                        ConnectionService connectionService,
                        LocationService locationService,
                        NamedLocationService namedLocationService) {
                this.userService = userService;
                this.purposeService = purposeService;
                this.personalityService = personalityService;
                this.interestService = interestService;
                this.genderService = genderService;
                this.nationalityService = nationalityService;
                this.locationService = locationService;
                this.namedLocationService = namedLocationService;
        }

        @PostMapping("/createInterests")
        public ResponseEntity<Object> createInterests(@RequestBody Set<InterestDTO> userData) {
                System.err.println("Creating interests");
                interestService.createInterest(userData);
                return ResponseEntity.status(HttpStatus.OK).body("Successfully created interests data");
        }

        @PostMapping("/createPersonalities")
        public ResponseEntity<Object> createPersonalities(@RequestBody Set<PersonalityDTO> userData) {
                System.err.println("Creating personalities");
                personalityService.createPersonality(userData);
                return ResponseEntity.status(HttpStatus.OK).body("Successfully created personalities data");
        }

        @PostMapping("/createPurposes")
        public ResponseEntity<Object> createPurposes(@RequestBody Set<PurposeDTO> userData) {
                System.err.println("Creating purposes");
                purposeService.createPurpose(userData);
                return ResponseEntity.status(HttpStatus.OK).body("Successfully created purposes data");
        }

        @PostMapping("/createGenders")
        public ResponseEntity<Object> createGenders(@RequestBody Set<GenderDTO> userData) {
                System.err.println("Creating Genders");
                genderService.createGender(userData);
                return ResponseEntity.status(HttpStatus.OK).body("Successfully created genres data");
        }

        @PostMapping("/createNationalities")
        public ResponseEntity<Object> createNationalities(@RequestBody Set<NationalityDTO> userData) {
                System.err.println("Creating Nationalities");
                nationalityService.createNationality(userData);
                return ResponseEntity.status(HttpStatus.OK).body("Successfully created nationalities data");
        }

        @PostMapping("/createNamedLocations")
        public ResponseEntity<Object> createNamedLocations(@RequestBody Set<NamedLocationDTO> userData) {
                System.err.println("Creating Locations");
                namedLocationService.createNamedLocations(userData);
                return ResponseEntity.status(HttpStatus.OK).body("Successfully created locations data");
        }

        // @PostMapping("/createUser")
        // public ResponseEntity<Object> createUser(@RequestBody UserDTO userData) {
        // System.err.println("Creating user data @ entity controller");
        // UserDTO createdUser = userService.createUser(userData);
        // return ResponseEntity.status(HttpStatus.OK)
        // .body("Successfully created complete user: " + createdUser.getUsername());
        // }

        // @PostMapping("/createConnection")
        // public ResponseEntity<Object> createConnection(@RequestBody ConnectionDTO
        // connectionData) {
        // Connection createdConnectionDTO =
        // connectionService.createConnection(connectionData);
        // return ResponseEntity.status(HttpStatus.OK).body("Successfully created
        // Connection between users: "
        // + createdConnectionDTO.getSender() + " and " +
        // createdConnectionDTO.getReceiver());
        // }

        // @PostMapping("/createChatMessage")
        // public ResponseEntity<Object> createChatMessage(@RequestBody ChatMessageDTO
        // messageData) {
        // ChatMessage createdChatMessage =
        // chatMessageService.createChatMessage(messageData);
        // return ResponseEntity.status(HttpStatus.OK).body("Successfully created
        // Message between users: "
        // + createdChatMessage.getSender() + " and " +
        // createdChatMessage.getReceiver());
        // }

        @PostMapping("/createLocation")
        public ResponseEntity<Map<String, Object>> createLocation(@AuthenticationPrincipal UserDetailsImpl userDetails,
                        @RequestBody LocationDTO location) {
                LocationDTO createdLocation = LocationMapper.toDTO(locationService.createLocation(location));
                Map<String, Object> response = new HashMap<>();
                response.put("location", createdLocation);
                response.put("message", "Location successfully created with ID : " + createdLocation.getId());
                return ResponseEntity.status(HttpStatus.OK)
                                .body(response);
        }

        // @GetMapping("/getEntity/{id}")
        // public ResponseEntity<Object> getEntityById(
        // @AuthenticationPrincipal UserDetailsImpl userDetails,
        // @PathVariable Long id) {
        // ChatMessageDTO receivedDTO = chatMessageService.getChatMessageById(id);
        // return ResponseEntity.status(HttpStatus.OK)
        // .body("Retrieved from database with id: " + receivedDTO.getId() + " and
        // TimeStamp: "
        // + receivedDTO.getTimestamp() + " and content: "
        // + receivedDTO.getContent()
        // + receivedDTO.getSender() + receivedDTO.getReceiver());
        // }

        @GetMapping("/me")
        public ResponseEntity<Object> getResponse(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
                User currentUser = userService.getUserByEmail(userDetails);
                return ResponseEntity.status(HttpStatus.OK)
                                .body("Request received, token valid for: \n  email: " + currentUser.getEmail()
                                                + "\n  id: "
                                                + currentUser.getId() + "\n  username: " + currentUser.getUsername());
        }

        // @PutMapping("/updateEntity")
        // public ResponseEntity<Object> updateEntityDetails(
        // @AuthenticationPrincipal UserDetailsImpl userDetails,
        // @RequestBody ChatMessageDTO userData) {
        // ChatMessageDTO updatedDTO = chatMessageService.updateChatMessage(userData);
        // return ResponseEntity.status(HttpStatus.OK)
        // .body("Successfully updated data: " + updatedDTO.getId() + " and ChatMessage:
        // "
        // + updatedDTO.getTimestamp());
        // }

        // @DeleteMapping("/deleteEntity/{id}")
        // public ResponseEntity<Object> deleteEntityById(
        // @AuthenticationPrincipal UserDetailsImpl userDetails,
        // @PathVariable Long id) {
        // chatMessageService.deleteChatMessageById(id);
        // return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted entity
        // with id: " + id);
        // }
}
