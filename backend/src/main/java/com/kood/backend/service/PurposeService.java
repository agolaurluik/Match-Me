package com.kood.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kood.backend.dto.EntityDTOs.PurposeDTO;
import com.kood.backend.entity.Entities.Purpose;
import com.kood.backend.exceptions.BadRequestException;
import com.kood.backend.exceptions.DuplicateResourceException;
import com.kood.backend.exceptions.NotFoundException;
import com.kood.backend.mapper.PurposeMapper;
import com.kood.backend.repository.PurposeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurposeService {

    private final PurposeRepository purposeRepository;

    // Create
    public void createPurpose(PurposeDTO purpose) {
        if (purpose.getName() != null) {
            Purpose createdPurpose = new Purpose();
            createdPurpose.setName(purpose.getName());

            Optional<Purpose> existingPurpose = purposeRepository
                    .findByName(createdPurpose.getName());
            if (existingPurpose.isPresent()) {
                Purpose existing = existingPurpose.get();
                throw new DuplicateResourceException(
                        "Purpose " + existing.getName() + "  already exists with the id: " + existing.getId());
            }
            purposeRepository.save(createdPurpose);
        } else {
            throw new NotFoundException("Purpose is null or missing, please add a valid <String> in your request");
        }
    }

    public void createPurpose(Set<PurposeDTO> newPurposes) {
        if (newPurposes == null || newPurposes.isEmpty()) {
            throw new NotFoundException(
                    "Purposes input set is null or empty, please provide valid purposes.");
        }
        Set<PurposeDTO> toCreatePurposes = checkPurposes(newPurposes);
        if (toCreatePurposes.isEmpty()) {
            throw new BadRequestException("All provided Purposes already exist — nothing new to create.");
        }
        for (PurposeDTO p : toCreatePurposes) {
            createPurpose(p);
        }
    }

    public Set<PurposeDTO> checkPurposes(Set<PurposeDTO> newPurposes) {
        Set<PurposeDTO> toCreatePurposes = new HashSet<>();
        for (PurposeDTO Purpose : newPurposes) {
            Optional<Purpose> existingPurpose = purposeRepository.findByName(Purpose.getName());
            if (!existingPurpose.isPresent()) {
                toCreatePurposes.add(Purpose);
            }
        }
        return toCreatePurposes;
    }

    // Retrieve
    public PurposeDTO getPurposeDTOById(Long id) {
        Optional<Purpose> existingPurpose = purposeRepository.findById(id);
        if (existingPurpose.isPresent()) {
            PurposeDTO foundPurposeDTO = PurposeMapper.toDTO(existingPurpose.get());
            return foundPurposeDTO;
        }
        throw new BadRequestException("Purpose with the id: " + id + "not found.");
    }

    public Purpose getPurposeById(Long id) {
        Optional<Purpose> existingPurpose = purposeRepository.findById(id);
        if (existingPurpose.isPresent()) {
            Purpose foundPurposeDTO = existingPurpose.get();
            return foundPurposeDTO;
        }
        throw new BadRequestException("Purpose with the id: " + id + "not found.");
    }

    public Purpose getPurposeByName(String name) {
        Optional<Purpose> existingPurpose = purposeRepository.findByName(name);
        if (existingPurpose.isPresent()) {
            Purpose foundPurposeDTO = existingPurpose.get();
            return foundPurposeDTO;
        }
        throw new BadRequestException("Purpose with the name: " + name + "not found.");
    }

    public Set<Purpose> checkAllPurposes(Set<Purpose> incomingPurposes) {
        Set<Purpose> retrievedPurposes = new HashSet<>();
        if (incomingPurposes != null) {
            for (Purpose p : incomingPurposes) {
                Purpose existingPurpose = getPurposeById(p.getId());
                retrievedPurposes.add(existingPurpose);
            }
        }
        return retrievedPurposes;
    }

    public Set<PurposeDTO> getAllPurposes() {
        Set<PurposeDTO> allPurposes = new HashSet<>();
        List<Purpose> retrievedPurposes = purposeRepository.findAll();
        for (Purpose p : retrievedPurposes) {
            PurposeDTO pDTO = PurposeMapper.toDTO(p);
            allPurposes.add(pDTO);
        }
        return allPurposes;
    }

    // Update

    @Transactional
    public PurposeDTO updatePurpose(PurposeDTO incomingPurpose) {
        if (incomingPurpose.getId() != null) {
            Optional<Purpose> existingPurpose = purposeRepository.findById(incomingPurpose.getId());
            if (existingPurpose.isPresent()) {
                Purpose updatedPurpose = existingPurpose.get();
                updatedPurpose.setName(incomingPurpose.getName());
                purposeRepository.save(updatedPurpose);
                return PurposeMapper.toDTO(updatedPurpose);
            } else {
                createPurpose(incomingPurpose);
                return incomingPurpose;
            }
        } else {
            createPurpose(incomingPurpose);
            return incomingPurpose;
        }
    }

    // Delete
    public void deletePurposeById(Long id) {
        if (purposeRepository.existsById(id)) {
            purposeRepository.deleteById(id);
        } else {
            throw new NotFoundException("No Purpose found with the id: " + id);
        }
    }
}
