package com.kood.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.kood.backend.dto.EntityDTOs.InterestDTO;
import com.kood.backend.entity.Entities.Interest;
import com.kood.backend.exceptions.BadRequestException;
import com.kood.backend.exceptions.DuplicateResourceException;
import com.kood.backend.exceptions.NotFoundException;
import com.kood.backend.mapper.InterestMapper;
import com.kood.backend.repository.InterestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository InterestRepository;

    // Create
    public void createInterest(InterestDTO newInterest) {
        if (newInterest.getName() != null) {

            Interest createdInterest = new Interest();
            createdInterest.setName(newInterest.getName());
            Optional<Interest> existingInterest = InterestRepository.findByName(createdInterest.getName());

            if (existingInterest.isPresent()) {
                Interest existing = existingInterest.get();
                throw new DuplicateResourceException(
                        "Interest " + existing.getName() + " already exists with the id: " + existing.getId());
            }
            InterestRepository.save(createdInterest);
        } else {
            throw new NotFoundException("Interest is null or missing, please add a valid <String> in your request");
        }
    }

    public void createInterest(Set<InterestDTO> newInterests) {
        if (newInterests == null || newInterests.isEmpty()) {
            throw new NotFoundException(
                    "Interests input set is null or empty, please provide valid interests.");
        }
        Set<InterestDTO> toCreateInterests = checkInterests(newInterests);
        if (toCreateInterests.isEmpty()) {
            throw new BadRequestException("All provided Interests already exist — nothing new to create.");
        }
        for (InterestDTO i : toCreateInterests) {
            createInterest(i);
        }
    }

    public Set<InterestDTO> checkInterests(Set<InterestDTO> newInterests) {
        Set<InterestDTO> toCreateInterests = new HashSet<>();
        for (InterestDTO interest : newInterests) {
            Optional<Interest> existingInterest = InterestRepository.findByName(interest.getName());
            if (!existingInterest.isPresent()) {
                toCreateInterests.add(interest);
            }
        }
        return toCreateInterests;
    }

    // Retrieve
    public InterestDTO getInterestDTOById(@NonNull Long id) {
        Optional<Interest> existingInterest = InterestRepository.findById(id);
        if (existingInterest.isPresent()) {
            InterestDTO foundInterestDTO = InterestMapper.toDTO(existingInterest.get());
            return foundInterestDTO;
        }
        throw new BadRequestException("Interest with the id: " + id + "not found.");
    }

    public Interest getInterestById(@NonNull Long id) {
        Optional<Interest> existingInterest = InterestRepository.findById(id);
        if (existingInterest.isPresent()) {
            Interest foundInterest = existingInterest.get();
            return foundInterest;
        }
        throw new BadRequestException("Interest with the id: " + id + "not found.");
    }

    public Set<Interest> getInterestsByIds(Set<Long> ids) {
        Set<Interest> retrievedInterests = new HashSet<>();
        if (ids != null && !ids.isEmpty()) {
            for (Long i : ids) {
                if (i == null)
                    continue;
                Interest existingInterest = getInterestById(i);
                retrievedInterests.add(existingInterest);
            }
        }
        return retrievedInterests;
    }

    public Set<Interest> checkAllInterests(Set<Interest> incomingInterests) {
        Set<Interest> retrievedInterests = new HashSet<>();
        if (incomingInterests != null) {
            for (Interest i : incomingInterests) {
                Long interestId = i.getId();
                if (interestId == null)
                    continue;
                Interest existingInterest = getInterestById(interestId);
                retrievedInterests.add(existingInterest);
            }
        }
        return retrievedInterests;
    }

    public Set<InterestDTO> getAllInterests() {
        Set<InterestDTO> allInterests = new HashSet<>();
        List<Interest> retrievedInterests = InterestRepository.findAll();
        for (Interest i : retrievedInterests) {
            InterestDTO iDTO = InterestMapper.toDTO(i);
            allInterests.add(iDTO);
        }
        return allInterests;
    }

    // Update

    @Transactional
    public InterestDTO updateInterest(InterestDTO incomingInterest) {
        Long incomingInterestId = incomingInterest.getId();
        if (incomingInterestId != null) {
            Optional<Interest> existingInterest = InterestRepository.findById(incomingInterestId);
            if (existingInterest.isPresent()) {
                Interest updatedInterest = existingInterest.get();
                updatedInterest.setName(incomingInterest.getName());
                InterestRepository.save(updatedInterest);
                return InterestMapper.toDTO(updatedInterest);
            } else {
                createInterest(incomingInterest);
                return incomingInterest;
            }
        } else {
            createInterest(incomingInterest);
            return incomingInterest;
        }
    }

    // Delete
    public void deleteInterestById(@NonNull Long id) {
        if (InterestRepository.existsById(id)) {
            InterestRepository.deleteById(id);
        } else {
            throw new NotFoundException("No Interest found with the id: " + id);
        }
    }

}
