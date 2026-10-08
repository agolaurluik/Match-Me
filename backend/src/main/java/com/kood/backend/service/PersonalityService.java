package com.kood.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.kood.backend.dto.EntityDTOs.PersonalityDTO;
import com.kood.backend.entity.Entities.Personality;
import com.kood.backend.exceptions.BadRequestException;
import com.kood.backend.exceptions.DuplicateResourceException;
import com.kood.backend.exceptions.NotFoundException;
import com.kood.backend.mapper.PersonalityMapper;
import com.kood.backend.repository.PersonalityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonalityService {

    private final PersonalityRepository personalityRepository;

    // Create
    public void createPersonality(PersonalityDTO newPersonality) {
        if (newPersonality.getName() != null) {
            Personality createdPersonality = new Personality();
            createdPersonality.setName(newPersonality.getName());

            Optional<Personality> existingPersonality = personalityRepository
                    .findByName(createdPersonality.getName());
            if (existingPersonality.isPresent()) {
                Personality existing = existingPersonality.get();
                throw new DuplicateResourceException("Personality" + existing.getName()
                        + "  already exists with the id: " + existing.getId());
            }
            personalityRepository.save(createdPersonality);
        } else {
            throw new NotFoundException("Personality is null or missing, please add a valid <String> in your request");
        }
    }

    public void createPersonality(Set<PersonalityDTO> newPersonalities) {
        if (newPersonalities == null || newPersonalities.isEmpty()) {
            throw new NotFoundException(
                    "Personalities input set is null or empty, please provide valid personalities.");
        }
        Set<PersonalityDTO> toCreatePersonalities = checkPersonalities(newPersonalities);
        if (toCreatePersonalities.isEmpty()) {
            throw new BadRequestException("All provided Personalities already exist — nothing new to create.");
        }
        for (PersonalityDTO p : toCreatePersonalities) {
            createPersonality(p);
        }
    }

    public Set<PersonalityDTO> checkPersonalities(Set<PersonalityDTO> newPersonalities) {
        Set<PersonalityDTO> toCreatePersonalities = new HashSet<>();
        for (PersonalityDTO Personality : newPersonalities) {
            Optional<Personality> existingPersonality = personalityRepository.findByName(Personality.getName());
            if (!existingPersonality.isPresent()) {
                toCreatePersonalities.add(Personality);
            }
        }
        return toCreatePersonalities;
    }

    // Retrieve
    public PersonalityDTO getPersonalityDTOById(@NonNull Long id) {
        Optional<Personality> existingPersonality = personalityRepository.findById(id);
        if (existingPersonality.isPresent()) {
            PersonalityDTO foundPersonalityDTO = PersonalityMapper.toDTO(existingPersonality.get());
            return foundPersonalityDTO;
        }
        throw new BadRequestException("Personality with the id: " + id + "not found.");
    }

    public Personality getPersonalityById(@NonNull Long id) {
        Optional<Personality> existingPersonality = personalityRepository.findById(id);
        if (existingPersonality.isPresent()) {
            Personality p = existingPersonality.get();
            return p;
        }
        throw new BadRequestException("Personality with the id: " + id + "not found.");
    }

    public Set<Personality> getPersonalitiesByIds(Set<Long> ids) {
        Set<Personality> retrievedPersonalities = new HashSet<>();
        if (ids != null && !ids.isEmpty()) {
            for (Long i : ids) {
                if (i == null)
                    continue;
                Personality existingPersonality = getPersonalityById(i);
                retrievedPersonalities.add(existingPersonality);
            }
        }
        return retrievedPersonalities;
    }

    public Set<Personality> checkAllPersonalities(Set<Personality> incomingPersonalities) {
        Set<Personality> retrievedPersonalities = new HashSet<>();
        if (incomingPersonalities != null) {
            for (Personality p : incomingPersonalities) {
                Long personalityId = p.getId();
                if (personalityId != null) {
                    Personality existingPersonality = getPersonalityById(personalityId);
                    retrievedPersonalities.add(existingPersonality);
                }
            }
        }
        return retrievedPersonalities;
    }

    public Set<PersonalityDTO> getAllPersonalities() {
        Set<PersonalityDTO> allPersonalities = new HashSet<>();
        List<Personality> retrievedPersonalities = personalityRepository.findAll();
        for (Personality p : retrievedPersonalities) {
            PersonalityDTO pDTO = PersonalityMapper.toDTO(p);
            allPersonalities.add(pDTO);
        }
        return allPersonalities;
    }

    // Update

    @Transactional
    public PersonalityDTO updatePersonality(PersonalityDTO incomingPersonality) {
        Long incomingPersonalityId = incomingPersonality.getId();
        if (incomingPersonalityId != null) {
            Optional<Personality> existingPersonality = personalityRepository.findById(incomingPersonalityId);
            if (existingPersonality.isPresent()) {
                Personality updatedPersonality = existingPersonality.get();
                updatedPersonality.setName(incomingPersonality.getName());
                personalityRepository.save(updatedPersonality);
                return PersonalityMapper.toDTO(updatedPersonality);
            } else {
                createPersonality(incomingPersonality);
                return incomingPersonality;
            }
        } else {
            createPersonality(incomingPersonality);
            return incomingPersonality;
        }
    }

    // Delete
    public void deletePersonalityById(@NonNull Long id) {
        if (personalityRepository.existsById(id)) {
            personalityRepository.deleteById(id);
        } else {
            throw new NotFoundException("No Personality found with the id: " + id);
        }
    }

}
