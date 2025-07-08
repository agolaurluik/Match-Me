package com.kood.backend.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.kood.backend.dto.UserDTOs.UserBioDTO;
import com.kood.backend.dto.UserDTOs.UserDTO;
import com.kood.backend.dto.UserDTOs.UserFullDTO;
import com.kood.backend.dto.UserDTOs.UserMeProfileDTO;
import com.kood.backend.dto.UserDTOs.UserProfileDTO;
import com.kood.backend.dto.UserDTOs.UserSmallDTO;
import com.kood.backend.entity.Entities.Interest;
import com.kood.backend.entity.Entities.Personality;
import com.kood.backend.entity.UserEntities.User;
import com.kood.backend.service.AuthService;
import com.kood.backend.service.GenderService;
import com.kood.backend.service.InterestService;
import com.kood.backend.service.LocationService;
import com.kood.backend.service.NationalityService;
import com.kood.backend.service.PersonalityService;
import com.kood.backend.service.PurposeService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {

    private final GenderService genderService;
    private final InterestService interestService;
    private final NationalityService nationalityService;
    private final PurposeService purposeService;
    private final PersonalityService personalityService;
    private final LocationService locationService;
    private final AuthService authService;

    @Override
    public UserDTO toDTO(User user) { // This will return full object anyway because Lombok and Jackson auto-serialize
                                      // it
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profileImageName(user.getProfileImageName())
                .build();
    }

    @Override
    public UserSmallDTO toSmallDTO(User user) {
        return UserSmallDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profileImageName(user.getProfileImageName())
                .build();
    }

    @Override
    public UserProfileDTO toProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .userDescription(user.getUserDescription())
                .build();
    }

    @Override
    public UserMeProfileDTO toMeProfileDTO(User user) {
        return UserMeProfileDTO.builder()
                .id(user.getId())
                .userDescription(user.getUserDescription())
                .email(user.getEmail())
                .build();
    }

    @Override
    public UserBioDTO toBioDTO(User user) {
        UserBioDTO.UserBioDTOBuilder builder = UserBioDTO.builder()
                .id(user.getId());

        if (user.getBirthDate() != null) {
            builder.birthDate(user.getBirthDate());
        }
        if (user.getGender() != null) {
            builder.genderId(user.getGender().getId());
        }
        if (user.getNationality() != null) {
            builder.nationalityId(user.getNationality().getId());
        }

        if (user.getPersonalities() != null && !user.getPersonalities().isEmpty()) {
            Set<Long> personalityIds = user.getPersonalities().stream()
                    .map(Personality::getId)
                    .collect(Collectors.toSet());
            builder.personalityIds(personalityIds);
        }

        if (user.getInterests() != null && !user.getInterests().isEmpty()) {
            Set<Long> interestIds = user.getInterests().stream()
                    .map(Interest::getId)
                    .collect(Collectors.toSet());
            builder.interestIds(interestIds);
        }

        if (user.getPurpose() != null) {
            builder.purposeId(user.getPurpose().getId());
        }
        if (user.getLocation() != null) {
            builder.locationId(user.getLocation().getId());
        }

        return builder.build();
    }

    @Override
    public UserFullDTO toFullDTO(User user) {
        UserFullDTO.UserFullDTOBuilder builder = UserFullDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .lastSeen(user.getLastSeen())
                .profileImageName(user.getProfileImageName())
                .userDescription(user.getUserDescription());

        if (user.getBirthDate() != null) {
            builder.birthDate(user.getBirthDate());
        }
        if (user.getGender() != null) {
            builder.genderId(user.getGender().getId());
        }
        if (user.getNationality() != null) {
            builder.nationalityId(user.getNationality().getId());
        }

        if (user.getPersonalities() != null && !user.getPersonalities().isEmpty()) {
            Set<Long> personalityIds = user.getPersonalities().stream()
                    .map(Personality::getId)
                    .collect(Collectors.toSet());
            builder.personalityIds(personalityIds);
        }

        if (user.getInterests() != null && !user.getInterests().isEmpty()) {
            Set<Long> interestIds = user.getInterests().stream()
                    .map(Interest::getId)
                    .collect(Collectors.toSet());
            builder.interestIds(interestIds);
        }

        if (user.getPurpose() != null) {
            builder.purposeId(user.getPurpose().getId());
        }
        if (user.getLocation() != null) {
            builder.locationId(user.getLocation().getId());
        }

        return builder.build();
    }

    @Override
    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = new User(
                userDTO.getId(),
                userDTO.getUsername(),
                userDTO.getEmail(),
                userDTO.getBirthDate(),
                userDTO.getLastSeen(),
                genderService.getGenderById(userDTO
                        .getGenderId()),
                nationalityService.getNationalityById(userDTO
                        .getNationalityId()),
                userDTO.getPasswordHash(),
                userDTO.getProfileImageName(),
                personalityService.getPersonalitiesByIds(userDTO.getPersonalityIds()),
                locationService.getUserLocationByUserId(userDTO.getId()),
                interestService.getInterestsByIds(userDTO.getInterestIds()),
                authService.getMatchingFilterByUserId(userDTO.getId()),
                purposeService.getPurposeById(userDTO.getPurposeId()),
                userDTO.getUserDescription());
        return user;
    }
}
