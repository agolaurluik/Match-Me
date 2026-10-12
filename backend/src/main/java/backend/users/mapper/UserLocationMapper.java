package backend.users.mapper;

import backend.users.dto.UserLocationDTO;
import backend.users.entity.UserLocation;

public interface UserLocationMapper {
    UserLocation toEntity(UserLocationDTO UserLocationDTO);

    UserLocationDTO toDTO(UserLocation user);
}
