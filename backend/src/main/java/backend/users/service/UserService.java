package backend.users.service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.genericData.entity.Gender;
import backend.genericData.entity.Interest;
import backend.genericData.entity.Nationality;
import backend.genericData.entity.Personality;
import backend.genericData.entity.Purpose;
import backend.genericData.repository.GenderRepository;
import backend.genericData.repository.NationalityRepository;
import backend.genericData.service.InterestService;
import backend.genericData.service.PersonalityService;
import backend.genericData.service.PurposeService;
import backend.matching.dto.UserMatchingFilterDTO;
import backend.matching.entity.MatchingFilter;
import backend.matching.mapper.UserMatchingFilterMapperImpl;
import backend.matching.repository.MatchingFilterRepository;
import backend.users.dto.UserDTO;
import backend.users.dto.UserFullDTO;
import backend.users.entity.User;
import backend.users.mapper.UserMapper;
import backend.users.repository.UserRepository;
import backend.utilities.HelperMethods.DateUtilities;
import backend.utilities.exceptions.BadRequestException;
import backend.utilities.exceptions.NotFoundException;
import backend.utilities.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GenderRepository genderRepository;
    private final NationalityRepository nationalityRepository;
    private final MatchingFilterRepository matchingFilterRepository;

    private final PersonalityService personalityService;
    private final InterestService interestService;
    private final PurposeService purposeService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserMatchingFilterMapperImpl userMatchingFilterMapperImpl;

    // Retrieve
    public User getUserById(@NonNull Long id) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User foundUser = existingUser.get();
            return foundUser;
        }
        throw new BadRequestException("User with the id: " + id + " not found.");
    }

    public void getUserByIdCheck(@NonNull Long id) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            throw new BadRequestException("User with the id: " + id + " already exists.");
        }
    }

    public User getUserByEmail(UserDetails userDetails) {
        Optional<User> existingUser = userRepository.findByEmail(userDetails.getUsername());
        if (existingUser.isPresent()) {
            User foundUser = existingUser.get();
            return foundUser;
        } else {
            throw new NotFoundException(
                    "No user found in the database with the credentials: " + userDetails.getUsername());
        }
    }

    public User getMockUserByEmail(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User foundUser = existingUser.get();
            return foundUser;
        } else {
            throw new NotFoundException(
                    " No MockUser found in the database with the credentials: " + email);
        }
    }

    public UserMatchingFilterDTO getUserMatchingFilter(UserDetailsImpl userDetails) {
        Optional<MatchingFilter> existingFilter = matchingFilterRepository.findByUserId(userDetails.getId());
        if (existingFilter.isPresent()) {
            return userMatchingFilterMapperImpl.toDTO(existingFilter.get());
        } else {
            throw new NotFoundException("Matching filter not found for user ID: " + userDetails.getId());
        }
    }

    public MatchingFilter getUserMatchingFilterByUserId(Long id) {
        Optional<MatchingFilter> existingFilter = matchingFilterRepository.findByUserId(id);
        if (existingFilter.isPresent()) {
            return existingFilter.get();
        } else {
            throw new NotFoundException("Matching filter not found for user ID: " + id);
        }
    }

    public String getUserProfileImageNameByUserId(Long id) {

        if (id == null) {
            throw new BadRequestException("Id not provided for fetching profile image name");
        }

        String profileImageName = getUserById(id).getProfileImageName();
        if (!profileImageName.equals(null)) {
            return profileImageName;
        } else {
            throw new NotFoundException("Profile image name not found for user ID: " + id);
        }
    }

    // Update
    @Transactional
    public UserFullDTO updateProfile(UserDetailsImpl userDetails, UserDTO userData) {
        User existingUser = getUserById(Objects.requireNonNull(userDetails.getId()));
        if (existingUser != null) {
            if (userData.getUsername() != null && !userData.getUsername().isBlank()) {
                String incomingUsername = userData.getUsername();
                // Check username here
                existingUser.setUsername(incomingUsername);
            }

            if (userData.getBirthDate() != null) {
                Instant incomingBirthDate = userData.getBirthDate();
                boolean checkBirthDate = DateUtilities.validateBirthdate(incomingBirthDate);
                if (checkBirthDate) {
                    existingUser.setBirthDate(incomingBirthDate);
                }
            }

            if (userData.getGenderId() != null) {
                Long incomingGender = userData.getGenderId();
                if (incomingGender != null) {
                    Optional<Gender> checkGender = genderRepository.findById(incomingGender);
                    if (checkGender.isPresent()) {
                        existingUser.setGender(checkGender.get());
                    }
                }

            }

            if (userData.getNationalityId() != null) {
                Long incomingNationality = userData.getNationalityId();
                if (incomingNationality != null) {
                    Optional<Nationality> checkNationality = nationalityRepository.findById(incomingNationality);
                    if (checkNationality.isPresent()) {
                        existingUser.setNationality(checkNationality.get());
                    }
                }
            }

            if (userData.getPersonalityIds() != null && !userData.getPersonalityIds().isEmpty()) {
                Set<Personality> incomingPersonalities = personalityService
                        .getPersonalitiesByIds(userData.getPersonalityIds());

                existingUser.setPersonalities(incomingPersonalities);
            }

            if (userData.getInterestIds() != null && !userData.getInterestIds().isEmpty()) {
                Set<Interest> incomingInterests = interestService.getInterestsByIds(userData.getInterestIds());
                existingUser.setInterests(incomingInterests);
            }
            Long purposeId = userData.getPurposeId();
            if (purposeId != null) {
                Purpose incomingPurpose = purposeService.getPurposeById(purposeId);
                existingUser.setPurpose(incomingPurpose);
            }

            if (userData.getUserDescription() != null) {
                existingUser.setUserDescription(userData.getUserDescription());
            }

            userRepository.save(existingUser);
            System.out.println("User data updated in database for id: " + existingUser.getId());
            UserFullDTO savedUser = userMapper.toFullDTO(existingUser);
            return savedUser;
        } else {
            throw new UsernameNotFoundException("User with email :" + userData.getEmail() + " - not found");
        }
    }

    // Delete
    public void deleteUserById(@NonNull Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new NotFoundException("No User found with the id: " + id);
        }
    }
}
