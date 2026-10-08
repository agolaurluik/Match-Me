package com.kood.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.kood.backend.dto.EntityDTOs.NationalityDTO;
import com.kood.backend.entity.Entities.Nationality;
import com.kood.backend.exceptions.BadRequestException;
import com.kood.backend.exceptions.DuplicateResourceException;
import com.kood.backend.exceptions.NotFoundException;
import com.kood.backend.mapper.NationalityMapper;
import com.kood.backend.repository.NationalityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NationalityService {

    private final NationalityRepository nationalityRepository;

    // Create
    public void createNationality(NationalityDTO newNationality) {
        if (newNationality.getName() != null) {

            Nationality createdNationality = new Nationality();
            createdNationality.setName(newNationality.getName());
            Optional<Nationality> existingNationality = nationalityRepository.findByName(createdNationality.getName());

            if (existingNationality.isPresent()) {
                Nationality existing = existingNationality.get();
                throw new DuplicateResourceException( // I want to add an error collector so if a massive list is added,
                                                      // each error is shown together, not by one by one
                        "Nationality " + existing.getName() + " already exists with the id: " + existing.getId());
            }
            nationalityRepository.save(createdNationality);
        } else {
            throw new NotFoundException("Nationality is null or missing, please add a valid <String> in your request");
        }
    }

    public void createNationality(Set<NationalityDTO> newNationalities) {
        if (newNationalities == null || newNationalities.isEmpty()) {
            throw new NotFoundException(
                    "Nationalities input set is null or empty, please provide valid Nationalities.");
        }
        Set<NationalityDTO> toCreateNationalities = checkNationalities(newNationalities);
        if (toCreateNationalities.isEmpty()) {
            throw new BadRequestException("All provided Nationalities already exist — nothing new to create.");
        }
        for (NationalityDTO n : toCreateNationalities) {
            createNationality(n);
        }
    }

    public Nationality checkNationality(Nationality incomingNationality) {
        Nationality retrievedNationality = new Nationality();
        Long incomingNationalityId = incomingNationality.getId();
        if (incomingNationality != null && incomingNationalityId != null) {
            retrievedNationality = getNationalityById(incomingNationalityId);
        }
        return retrievedNationality;
    }

    public Set<NationalityDTO> checkNationalities(Set<NationalityDTO> newNationalities) {
        Set<NationalityDTO> toCreateNationalities = new HashSet<>();
        for (NationalityDTO nationality : newNationalities) {
            Optional<Nationality> existingNationality = nationalityRepository.findByName(nationality.getName());
            if (!existingNationality.isPresent()) {
                toCreateNationalities.add(nationality);
            }
        }
        return toCreateNationalities;
    }

    // Retrieve
    public NationalityDTO getNationalityDTOById(@NonNull Long id) {
        Optional<Nationality> existingNationality = nationalityRepository.findById(id);
        if (existingNationality.isPresent()) {
            NationalityDTO foundNationalityDTO = NationalityMapper.toDTO(existingNationality.get());
            return foundNationalityDTO;
        }
        throw new BadRequestException("Nationality with the id: " + id + "not found.");
    }

    public Nationality getNationalityById(@NonNull Long id) {
        Optional<Nationality> existingNationality = nationalityRepository.findById(id);
        if (existingNationality.isPresent()) {
            Nationality foundNationality = existingNationality.get();
            return foundNationality;
        }
        throw new BadRequestException("Nationality with the id: " + id + "not found.");
    }

    public Nationality getNationalityByName(String name) {
        Optional<Nationality> existingNationality = nationalityRepository.findByName(name);
        if (existingNationality.isPresent()) {
            Nationality foundNationality = existingNationality.get();
            return foundNationality;
        }
        throw new BadRequestException("Nationality with the name: " + name + "not found.");
    }

    public Set<NationalityDTO> getAllNationalities() {
        Set<NationalityDTO> allNationalities = new HashSet<>();
        List<Nationality> retrievedNationalities = nationalityRepository.findAll();
        for (Nationality n : retrievedNationalities) {
            NationalityDTO nDTO = NationalityMapper.toDTO(n);
            allNationalities.add(nDTO);
        }
        return allNationalities;
    }

    // Update

    @Transactional
    public NationalityDTO updateNationality(NationalityDTO incomingNationality) {
        Long incomingNationalityId = incomingNationality.getId();
        if (incomingNationality.getId() != null && incomingNationalityId != null) {
            Optional<Nationality> existingNationality = nationalityRepository.findById(incomingNationalityId);
            if (existingNationality.isPresent()) {
                Nationality updatedNationality = existingNationality.get();
                updatedNationality.setName(incomingNationality.getName());
                nationalityRepository.save(updatedNationality);
                return NationalityMapper.toDTO(updatedNationality);
            } else {
                createNationality(incomingNationality);
                return incomingNationality;
            }
        } else {
            createNationality(incomingNationality);
            return incomingNationality;
        }
    }

    // Delete
    public void deleteNationalityById(@NonNull Long id) {
        if (nationalityRepository.existsById(id)) {
            nationalityRepository.deleteById(id);
        } else {
            throw new NotFoundException("No Nationality found with the id: " + id);
        }
    }

}
