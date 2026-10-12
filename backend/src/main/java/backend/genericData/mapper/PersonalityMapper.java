package backend.genericData.mapper;

import backend.genericData.dto.PersonalityDTO;
import backend.genericData.entity.Personality;

public interface PersonalityMapper {
    public static PersonalityDTO toDTO(Personality Personality) {
        if (Personality == null) {
            return null;
        }
        return PersonalityDTO.builder()
                .id(Personality.getId())
                .name(Personality.getName())
                .build();
    }

    public static Personality toEntity(PersonalityDTO PersonalityDTO) {
        if (PersonalityDTO == null) {
            return null;
        }
        Personality Personality = new Personality();
        Personality.setId(PersonalityDTO.getId());
        Personality.setName(PersonalityDTO.getName());
        return Personality;
    }
}