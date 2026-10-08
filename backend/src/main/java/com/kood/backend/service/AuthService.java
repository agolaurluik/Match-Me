package com.kood.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kood.backend.dto.AuthDTOs.LoginDTO;
import com.kood.backend.dto.AuthDTOs.RegisterDTO;
import com.kood.backend.entity.Entities.Gender;
import com.kood.backend.entity.UserEntities.MatchingFilter;
import com.kood.backend.entity.UserEntities.User;
import com.kood.backend.exceptions.EmailAlreadyExistsException;
import com.kood.backend.exceptions.EmailNotFoundException;
import com.kood.backend.repository.AuthRepository;
import com.kood.backend.repository.GenderRepository;
import com.kood.backend.repository.MatchingFilterRepository;
import com.kood.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthRepository authRepository;
    private final GenderRepository genderRepository;
    private final MatchingFilterRepository matchingFilterRepository;

    public User registerUser(RegisterDTO userData) {
        if (userRepository.existsByEmail(userData.getEmail())) {
            throw new EmailAlreadyExistsException("This email is already registered.");
        }

        String hashedPassword = passwordEncoder.encode(userData.getPassword());
        User user = new User();

        user.setEmail(userData.getEmail());
        user.setUsername(userData.getUsername());
        user.setPasswordHash(hashedPassword);
        user.setBirthDate((userData.getBirthDate()));

        Long userGenderId = userData.getGenderId();
        if (userGenderId != null) {
            Gender gender = genderRepository.findById(userGenderId)
                    .orElseThrow(() -> new RuntimeException("Gender not found with id: " + userData.getGenderId()));
            user.setGender(gender);
        } else {
            user.setGender(null);
        }

        // -----------------------------------------------------------
        String defaultImageName = ("default-user.jpg");
        user.setProfileImageName(defaultImageName);

        user.setPersonalities(null);
        user.setInterests(null);
        user.setPurpose(null);
        user.setNationality(null);
        user.setLocation(null);

        User savedUser = userRepository.save(user);
        MatchingFilter filter = new MatchingFilter();
        filter.setUserId(user.getId());

        filter.setMatch_limit(9);
        filter.setGenderPreference("lookingforall");
        filter.setNationalityScore(1);
        filter.setInterestScore(1);
        filter.setPersonalityScore(1);
        filter.setPurposeScore(1);
        filter.setHighestAge(100);
        filter.setLowestAge(0);
        filter.setRadius(10000);
        matchingFilterRepository.save(filter);
        return savedUser;
    }

    public MatchingFilter getMatchingFilterByUserId(Long userId) {
        try {
            Optional<MatchingFilter> optionalFilter = authRepository.findByUserId(userId);
            return optionalFilter.orElse(null); // returns null if not found
        } catch (Exception e) {
            return null;
        }
    }

    public User loginUser(LoginDTO loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new EmailNotFoundException("Invalid email or the e-mail does not exist"));

        boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash());
        if (!passwordMatches) {
            throw new RuntimeException("Invalid password.");
        }

        return user;
    }

    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

}