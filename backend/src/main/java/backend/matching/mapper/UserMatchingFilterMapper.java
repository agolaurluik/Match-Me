package backend.matching.mapper;

import backend.matching.dto.UserMatchingFilterDTO;
import backend.matching.entity.MatchingFilter;

public interface UserMatchingFilterMapper {
    UserMatchingFilterDTO toDTO(MatchingFilter entity);

    MatchingFilter toEntity(UserMatchingFilterDTO dto);
}