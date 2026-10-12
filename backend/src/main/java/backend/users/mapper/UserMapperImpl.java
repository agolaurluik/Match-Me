package backend.users.mapper;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import backend.genericData.service.GenderService;
import backend.genericData.service.InterestService;
import backend.genericData.service.LocationService;
import backend.genericData.service.NationalityService;
import backend.genericData.service.PersonalityService;
import backend.genericData.service.PurposeService;
import backend.users.dto.UserBioDTO;
import backend.users.dto.UserDTO;
import backend.users.dto.UserFullDTO;
import backend.users.dto.UserMeProfileDTO;
import backend.users.dto.UserProfileDTO;
import backend.users.dto.UserSmallDTO;
import backend.users.entity.User;
import backend.utilities.exceptions.BadRequestException;
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
        System.out.println("Inside converter user ID is: " + user.getId());
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
                    .map(Objects::requireNonNull)
                    .map(personality -> personality.getId())
                    .collect(Collectors.toSet());
            builder.personalityIds(personalityIds);
        }

        if (user.getInterests() != null && !user.getInterests().isEmpty()) {
            Set<Long> interestIds = user.getInterests().stream()
                    .map(Objects::requireNonNull)
                    .map(interest -> interest.getId())
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
                    .map(Objects::requireNonNull)
                    .map(personality -> personality.getId())
                    .collect(Collectors.toSet());
            builder.personalityIds(personalityIds);
        }

        if (user.getInterests() != null && !user.getInterests().isEmpty()) {
            Set<Long> interestIds = user.getInterests().stream()
                    .map(Objects::requireNonNull)
                    .map(interest -> interest.getId())
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
        Long userGenderId = userDTO.getGenderId();
        Long userNationalityId = userDTO.getNationalityId();
        Long userPurposeId = userDTO.getPurposeId();
        Long userId = userDTO.getId();
        if (userGenderId == null || userId == null || userNationalityId == null || userPurposeId == null)
            throw new BadRequestException("Null value in dto during user dto to entity conversion");
        User user = new User(
                userDTO.getId(),
                userDTO.getUsername(),
                userDTO.getEmail(),
                userDTO.getBirthDate(),
                userDTO.getLastSeen(),
                genderService.getGenderById(userGenderId),
                nationalityService.getNationalityById(userNationalityId),
                userDTO.getPasswordHash(),
                userDTO.getProfileImageName(),
                personalityService.getPersonalitiesByIds(userDTO.getPersonalityIds()),
                locationService.getUserLocationByUserId(userId),
                interestService.getInterestsByIds(userDTO.getInterestIds()),
                purposeService.getPurposeById(userPurposeId),
                userDTO.getUserDescription());
        return user;
    }
}
