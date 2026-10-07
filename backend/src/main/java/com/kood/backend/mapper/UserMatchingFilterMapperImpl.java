package com.kood.backend.mapper;

import org.springframework.stereotype.Component;

import com.kood.backend.dto.algorithmDTOs.UserMatchingFilterDTO;
import com.kood.backend.entity.UserEntities.MatchingFilter;
import com.kood.backend.exceptions.BadRequestException;
import com.kood.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMatchingFilterMapperImpl implements UserMatchingFilterMapper {

    private final UserRepository userRepository;

    @Override
    public UserMatchingFilterDTO toDTO(MatchingFilter entity) {
        if (entity == null)
            return null;

        return UserMatchingFilterDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .match_limit(entity.getMatch_limit())
                .genderPreference(entity.getGenderPreference())
                .interestScore(entity.getInterestScore())
                .nationalityScore(entity.getNationalityScore())
                .personalityScore(entity.getPersonalityScore())
                .purposeScore(entity.getPurposeScore())
                .lowestAge(entity.getLowestAge())
                .highestAge(entity.getHighestAge())
                .radius(entity.getRadius())
                .build();
    }

    @Override
    public MatchingFilter toEntity(UserMatchingFilterDTO dto) {
        if (dto == null)
            return null;

        MatchingFilter filter = new MatchingFilter();
        filter.setId(dto.getId());
        filter.setMatch_limit(dto.getMatch_limit());
        filter.setGenderPreference(dto.getGenderPreference());
        filter.setInterestScore(dto.getInterestScore());
        filter.setNationalityScore(dto.getNationalityScore());
        filter.setPersonalityScore(dto.getPersonalityScore());
        filter.setPurposeScore(dto.getPurposeScore());
        filter.setLowestAge(dto.getLowestAge());
        filter.setHighestAge(dto.getHighestAge());
        filter.setRadius(dto.getRadius());

        userRepository.findById(dto.getUserId())
                .ifPresentOrElse(
                        userId -> filter.setUserId(dto.getUserId()),
                        () -> {
                            throw new BadRequestException("User not found with id: " + dto.getUserId());
                        });

        return filter;
    }

}
