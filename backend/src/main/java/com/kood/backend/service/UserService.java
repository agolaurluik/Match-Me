package com.kood.backend.service;

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

import com.kood.backend.dto.UserDTOs.UserDTO;
import com.kood.backend.dto.UserDTOs.UserFullDTO;
import com.kood.backend.dto.algorithmDTOs.UserMatchingFilterDTO;
import com.kood.backend.entity.Entities.Gender;
import com.kood.backend.entity.Entities.Interest;
import com.kood.backend.entity.Entities.Nationality;
import com.kood.backend.entity.Entities.Personality;
import com.kood.backend.entity.Entities.Purpose;
import com.kood.backend.entity.UserEntities.MatchingFilter;
import com.kood.backend.entity.UserEntities.User;
import com.kood.backend.exceptions.BadRequestException;
import com.kood.backend.exceptions.NotFoundException;
import com.kood.backend.mapper.UserMapper;
import com.kood.backend.mapper.UserMatchingFilterMapperImpl;
import com.kood.backend.repository.GenderRepository;
import com.kood.backend.repository.MatchingFilterRepository;
import com.kood.backend.repository.NationalityRepository;
import com.kood.backend.repository.UserRepository;
import com.kood.backend.security.UserDetailsImpl;
import com.kood.backend.service.HelperMethods.DateUtilities;

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
