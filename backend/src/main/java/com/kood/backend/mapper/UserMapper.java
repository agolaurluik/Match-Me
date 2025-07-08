package com.kood.backend.mapper;

import com.kood.backend.dto.UserDTOs.UserBioDTO;
import com.kood.backend.dto.UserDTOs.UserDTO;
import com.kood.backend.dto.UserDTOs.UserFullDTO;
import com.kood.backend.dto.UserDTOs.UserMeProfileDTO;
import com.kood.backend.dto.UserDTOs.UserProfileDTO;
import com.kood.backend.dto.UserDTOs.UserSmallDTO;
import com.kood.backend.entity.UserEntities.User;

public interface UserMapper {
    User toEntity(UserDTO userDTO);

    UserDTO toDTO(User user);

    UserSmallDTO toSmallDTO(User user);

    UserBioDTO toBioDTO(User user);

    UserProfileDTO toProfileDTO(User user);

    UserMeProfileDTO toMeProfileDTO(User user);

    UserFullDTO toFullDTO(User user);
}