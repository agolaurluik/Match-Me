package backend.matching.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import backend.matching.entity.MatchingFilter;
import backend.matching.repository.MatchingFilterRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchingFilterRepository matchingFilterRepository;

    public MatchingFilter getMatchingFilterByUserId(Long userId) {
        try {
            Optional<MatchingFilter> optionalFilter = matchingFilterRepository.findByUserId(userId);
            return optionalFilter.orElse(null); // returns null if not found
        } catch (Exception e) {
            return null;
        }
    }
}
