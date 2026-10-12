package backend.genericData.mapper;

import backend.genericData.dto.GenderDTO;
import backend.genericData.entity.Gender;

public class GenderMapper {
    public static GenderDTO toDTO(Gender gender) {
        if (gender == null) {
            return null;
        }
        return GenderDTO.builder()
                .id(gender.getId())
                .name(gender.getName())
                .build();
    }

    public static Gender toEntity(GenderDTO genderDTO) {
        if (genderDTO == null) {
            return null;
        }
        Gender gender = new Gender();
        gender.setId(genderDTO.getId());
        gender.setName(genderDTO.getName());
        return gender;
    }
}
