package backend.users.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import backend.genericData.entity.Location;
import backend.genericData.repository.LocationRepository;
import backend.users.dto.UserLocationDTO;
import backend.users.entity.User;
import backend.users.entity.UserLocation;
import backend.users.repository.UserRepository;
import backend.utilities.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserLocationMapperImpl implements UserLocationMapper {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public UserLocationDTO toDTO(UserLocation location) {
        if (location == null) {
            return null;
        }
        return UserLocationDTO.builder()
                .id(location.getId())
                .locationId(location.getLocation().getId())
                .userId(location.getUser().getId())
                .build();
    }

    public UserLocation toEntity(UserLocationDTO locationDTO) {
        if (locationDTO == null) {
            return null;
        }
        UserLocation userLocation = new UserLocation();
        userLocation.setId(locationDTO.getLocationId());

        Long locationDTOId = locationDTO.getLocationId();
        if (locationDTOId == null) {
            throw new BadRequestException("Location id is null during entity conversion");
        }
        Optional<User> existingUser = userRepository.findById(locationDTOId);
        if (existingUser.isPresent()) {
            userLocation.setUser(existingUser.get());
        } else {
            throw new BadRequestException(
                    "User with the id: " + locationDTO.getId() + "not found during entity conversion.");
        }

        Optional<Location> existingLocation = locationRepository.findById(locationDTOId);
        if (existingLocation.isPresent()) {
            userLocation.setLocation(existingLocation.get());
        } else {
            throw new BadRequestException(
                    "Location with the id: " + locationDTO.getLocationId() + "not found during entity conversion.");
        }
        return userLocation;
    }
}
