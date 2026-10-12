package backend.users.mapper;

import backend.users.dto.UserBioDTO;
import backend.users.dto.UserDTO;
import backend.users.dto.UserFullDTO;
import backend.users.dto.UserMeProfileDTO;
import backend.users.dto.UserProfileDTO;
import backend.users.dto.UserSmallDTO;
import backend.users.entity.User;

public interface UserMapper {
    User toEntity(UserDTO userDTO);

    UserDTO toDTO(User user);

    UserSmallDTO toSmallDTO(User user);

    UserBioDTO toBioDTO(User user);

    UserProfileDTO toProfileDTO(User user);

    UserMeProfileDTO toMeProfileDTO(User user);

    UserFullDTO toFullDTO(User user);
}