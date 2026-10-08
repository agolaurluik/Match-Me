package com.kood.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.kood.backend.dto.EntityDTOs.GenderDTO;
import com.kood.backend.entity.Entities.Gender;
import com.kood.backend.exceptions.BadRequestException;
import com.kood.backend.exceptions.DuplicateResourceException;
import com.kood.backend.exceptions.NotFoundException;
import com.kood.backend.mapper.GenderMapper;
import com.kood.backend.repository.GenderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenderService {

    private final GenderRepository genderRepository;

    // Create
    public void createGender(GenderDTO newGender) {
        if (newGender.getName() != null) {

            Gender createdGender = new Gender();
            createdGender.setName(newGender.getName());
            Optional<Gender> existingGender = genderRepository.findByName(createdGender.getName());

            if (existingGender.isPresent()) {
                Gender existing = existingGender.get();
                throw new DuplicateResourceException(
                        "Gender " + existing.getName() + " already exists with the id: " + existing.getId());
            }
            genderRepository.save(createdGender);
        } else {
            throw new NotFoundException("Gender is null or missing, please add a valid <String> in your request");
        }
    }

    public void createGender(Set<GenderDTO> newGenders) {
        if (newGenders == null || newGenders.isEmpty()) {
            throw new NotFoundException(
                    "Genders input set is null or empty, please provide valid Genders.");
        }
        Set<GenderDTO> toCreateGenders = checkGenders(newGenders);
        if (toCreateGenders.isEmpty()) {
            throw new BadRequestException("All provided Genders already exist — nothing new to create.");
        }
        for (GenderDTO g : newGenders) {
            createGender(g);
        }
    }

    public Set<GenderDTO> checkGenders(Set<GenderDTO> newGenders) {
        Set<GenderDTO> toCreateGenders = new HashSet<>();
        for (GenderDTO gender : newGenders) {
            Optional<Gender> existingGender = genderRepository.findByName(gender.getName());
            if (!existingGender.isPresent()) {
                toCreateGenders.add(gender);
            }
        }
        return toCreateGenders;
    }

    // Retrieve
    public GenderDTO getGenderDTOById(@NonNull Long id) {
        Optional<Gender> existingGender = genderRepository.findById(id);
        if (existingGender.isPresent()) {
            GenderDTO foundGenderDTO = GenderMapper.toDTO(existingGender.get());
            return foundGenderDTO;
        }
        throw new BadRequestException("Gender with the id: " + id + "not found.");
    }

    public Gender getGenderById(@NonNull Long id) {
        Optional<Gender> existingGender = genderRepository.findById(id);
        if (existingGender.isPresent()) {
            Gender foundGender = existingGender.get();
            return foundGender;
        }
        throw new BadRequestException("Gender with the id: " + id + "not found.");
    }

    public Gender getGenderByName(String name) {
        Optional<Gender> existingGender = genderRepository.findByName(name);
        if (existingGender.isPresent()) {
            Gender foundGender = existingGender.get();
            return foundGender;
        }
        throw new BadRequestException("Gender with the name: " + name + "not found.");
    }

    public Gender checkGender(Gender incomingGender) {
        Gender existingGender = new Gender();
        Long incomingGenderId = incomingGender.getId();
        if (incomingGender != null && incomingGenderId != null) {
            existingGender = getGenderById(incomingGenderId);
        }
        return existingGender;
    }

    public Set<GenderDTO> getAllGenders() {
        Set<GenderDTO> allGenders = new HashSet<>();
        List<Gender> retrievedGenders = genderRepository.findAll();
        for (Gender g : retrievedGenders) {
            GenderDTO gDTO = GenderMapper.toDTO(g);
            allGenders.add(gDTO);
        }
        return allGenders;
    }

    // Update

    @Transactional
    public GenderDTO updateGender(GenderDTO incomingGender) {
        Long incomingGenderId = incomingGender.getId();
        if (incomingGenderId != null) {
            Optional<Gender> existingGender = genderRepository.findById(incomingGenderId);
            if (existingGender.isPresent()) {
                Gender updatedGender = existingGender.get();
                updatedGender.setName(incomingGender.getName());
                genderRepository.save(updatedGender);
                return GenderMapper.toDTO(updatedGender);
            } else {
                createGender(incomingGender);
                return incomingGender;
            }
        } else {
            createGender(incomingGender);
            return incomingGender;
        }
    }

    // Delete
    public void deleteGenderById(@NonNull Long id) {
        if (genderRepository.existsById(id)) {
            genderRepository.deleteById(id);
        } else {
            throw new NotFoundException("No Gender found with the id: " + id);
        }
    }
}
