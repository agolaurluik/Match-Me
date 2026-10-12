package backend.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import backend.auth.dto.LoginDTO;
import backend.auth.dto.RegisterDTO;
import backend.genericData.entity.Gender;
import backend.genericData.repository.GenderRepository;
import backend.matching.entity.MatchingFilter;
import backend.matching.repository.MatchingFilterRepository;
import backend.users.entity.User;
import backend.users.repository.UserRepository;
import backend.utilities.exceptions.EmailAlreadyExistsException;
import backend.utilities.exceptions.EmailNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
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