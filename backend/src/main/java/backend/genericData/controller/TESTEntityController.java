package backend.genericData.controller;

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

import backend.chat.service.ChatMessageService;
import backend.connection.service.ConnectionService;
import backend.genericData.dto.GenderDTO;
import backend.genericData.dto.InterestDTO;
import backend.genericData.dto.LocationDTO;
import backend.genericData.dto.NamedLocationDTO;
import backend.genericData.dto.NationalityDTO;
import backend.genericData.dto.PersonalityDTO;
import backend.genericData.dto.PurposeDTO;
import backend.genericData.mapper.LocationMapper;
import backend.genericData.service.GenderService;
import backend.genericData.service.InterestService;
import backend.genericData.service.LocationService;
import backend.genericData.service.NamedLocationService;
import backend.genericData.service.NationalityService;
import backend.genericData.service.PersonalityService;
import backend.genericData.service.PurposeService;
import backend.users.entity.User;
import backend.users.service.UserService;
import backend.utilities.security.UserDetailsImpl;

//////////////////////////////////////// THIS IS FOR TESTING AND CREATING DEFAULT VALUES
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

        @GetMapping("/me")
        public ResponseEntity<Object> getResponse(
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
                User currentUser = userService.getUserByEmail(userDetails);
                return ResponseEntity.status(HttpStatus.OK)
                                .body("Request received, token valid for: \n  email: " + currentUser.getEmail()
                                                + "\n  id: "
                                                + currentUser.getId() + "\n  username: " + currentUser.getUsername());
        }

}
