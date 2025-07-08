package com.kood.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.kood.backend.dto.LocationDTOs.LocationPoint;
import com.kood.backend.dto.LocationDTOs.NamedLocationDTO;
import com.kood.backend.entity.LocationEntities.NamedLocation;
import com.kood.backend.exceptions.BadRequestException;
import com.kood.backend.exceptions.NotFoundException;
import com.kood.backend.mapper.NamedLocationMapper;
import com.kood.backend.repository.NamedLocationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NamedLocationService {

    private final NamedLocationRepository namedLocationRepository;

    // Create
    public NamedLocation createNamedLocation(NamedLocationDTO newNamedLocation) {
        if (newNamedLocation.getPoint() != null) {

            LocationPoint coords = newNamedLocation.getPoint();
            Coordinate coordinate = new Coordinate(coords.getLongitude(), coords.getLatitude());
            Point point = new GeometryFactory().createPoint(coordinate);
            point.setSRID(4326);

            NamedLocation createdNamedLocation = new NamedLocation();
            createdNamedLocation.setName(newNamedLocation.getName());
            createdNamedLocation.setPoint(point);
            createdNamedLocation.setAccuracy(newNamedLocation.getAccuracy());

            namedLocationRepository.save(createdNamedLocation);
            return createdNamedLocation;
        } else {
            throw new NotFoundException(
                    "NamedLocation is null or missing, please add a valid <String> in your request");
        }
    }

    public void createNamedLocations(Set<NamedLocationDTO> newNamedLocations) {
        if (newNamedLocations == null || newNamedLocations.isEmpty()) {
            throw new NotFoundException(
                    "NamedLocations input set is null or empty, please provide valid Locations.");
        }
        Set<NamedLocationDTO> toCreateNamedLocation = checkNamedLocations(newNamedLocations);
        if (toCreateNamedLocation.isEmpty()) {
            throw new BadRequestException("All provided Locations already exist — nothing new to create.");
        }
        for (NamedLocationDTO l : toCreateNamedLocation) {
            createNamedLocation(l);
        }
    }

    public Set<NamedLocationDTO> checkNamedLocations(Set<NamedLocationDTO> incomingLocations) {
        Set<NamedLocationDTO> newNamedLocations = new HashSet<>();
        for (NamedLocationDTO location : incomingLocations) {
            Optional<NamedLocation> foundLocation = namedLocationRepository.findExistingLocation(
                    location.getPoint().getLongitude(),
                    location.getPoint().getLatitude(),
                    1000);
            if (!foundLocation.isPresent()) {
                newNamedLocations.add(location);
            }
        }
        return newNamedLocations;

    }

    // Retrieve
    public NamedLocationDTO getNamedLocationById(Long id) {
        Optional<NamedLocation> existingLocation = namedLocationRepository.findById(id);
        if (existingLocation.isPresent()) {
            NamedLocationDTO foundLocationDTO = NamedLocationMapper.toDTO(existingLocation.get());
            return foundLocationDTO;
        }
        throw new BadRequestException("NamedLocation with the id: " + id + "not found.");
    }

    public Set<NamedLocationDTO> getAllNamedLocations() {
        Set<NamedLocationDTO> allNamedLocations = new HashSet<>();
        List<NamedLocation> retrievedNamedLocations = namedLocationRepository.findAll();
        for (NamedLocation nl : retrievedNamedLocations) {
            NamedLocationDTO nlDTO = NamedLocationMapper.toDTO(nl);
            allNamedLocations.add(nlDTO);
        }
        return allNamedLocations;
    }
    // Update

    @Transactional
    public NamedLocationDTO updateNamedLocation(NamedLocationDTO incomingNamedLocationDTO) {
        if (incomingNamedLocationDTO.getId() != null) {
            Optional<NamedLocation> existingLocation = namedLocationRepository
                    .findById(incomingNamedLocationDTO.getId());
            if (existingLocation.isPresent()) {
                NamedLocation updatedLocation = existingLocation.get();

                LocationPoint coords = incomingNamedLocationDTO.getPoint();
                Coordinate coordinate = new Coordinate(coords.getLongitude(), coords.getLatitude());
                Point point = new GeometryFactory().createPoint(coordinate);
                point.setSRID(4326);
                updatedLocation.setPoint(point);

                namedLocationRepository.save(updatedLocation);
                return NamedLocationMapper.toDTO(updatedLocation);
            } else {
                createNamedLocation(incomingNamedLocationDTO);
                return incomingNamedLocationDTO;
            }
        } else {
            createNamedLocation(incomingNamedLocationDTO);
            return incomingNamedLocationDTO;
        }
    }

    // Delete
    public void deleteNamedLocationById(Long id) {
        if (namedLocationRepository.existsById(id)) {
            namedLocationRepository.deleteById(id);
        } else {
            throw new NotFoundException("No NamedLocation found with the id: " + id);
        }
    }
}
