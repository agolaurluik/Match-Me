package com.kood.backend.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.kood.backend.dto.LocationDTOs.LocationDTO;
import com.kood.backend.dto.LocationDTOs.LocationPoint;
import com.kood.backend.dto.LocationDTOs.UserLocationDTO;
import com.kood.backend.entity.LocationEntities.Location;
import com.kood.backend.entity.UserEntities.User;
import com.kood.backend.entity.UserEntities.UserLocation;
import com.kood.backend.exceptions.BadRequestException;
import com.kood.backend.exceptions.NotFoundException;
import com.kood.backend.exceptions.UserLocationDataAlreadyExistsException;
import com.kood.backend.mapper.LocationMapper;
import com.kood.backend.mapper.UserLocationMapper;
import com.kood.backend.repository.LocationRepository;
import com.kood.backend.repository.UserLocationRepository;
import com.kood.backend.repository.UserRepository;
import com.kood.backend.security.UserDetailsImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final UserLocationRepository userLocationRepository;
    private final UserLocationMapper userLocationMapper;

    // Create
    public Location createLocation(LocationDTO newLocation) {
        if (newLocation.getPoint() != null) {

            LocationPoint coords = newLocation.getPoint();
            Coordinate coordinate = new Coordinate(coords.getLongitude(), coords.getLatitude());
            Point point = new GeometryFactory().createPoint(coordinate);
            point.setSRID(4326);

            Location createdLocation = new Location();
            createdLocation.setPoint(point);
            createdLocation.setAccuracy(newLocation.getAccuracy());
            createdLocation.setTimestamp(newLocation.getTimestamp());

            locationRepository.save(createdLocation);
            locationRepository.flush();
            return createdLocation;
        } else {
            throw new NotFoundException("Location is null or missing, please add a valid <String> in your request");
        }
    }

    public void createLocations(Set<LocationDTO> newLocations) {
        if (newLocations == null || newLocations.isEmpty()) {
            throw new NotFoundException(
                    "Locations input set is null or empty, please provide valid Locations.");
        }
        Set<LocationDTO> toCreateLocationData = checkLocations(newLocations);
        if (toCreateLocationData.isEmpty()) {
            throw new BadRequestException("All provided Locations already exist — nothing new to create.");
        }
        for (LocationDTO l : toCreateLocationData) {
            createLocation(l);
        }
    }

    @Transactional
    public UserLocationDTO createUserLocation(LocationDTO location, UserDetailsImpl userDetails) {
        User dataBaseUser = new User();
        UserLocation newBinding = new UserLocation();

        Optional<User> foundUser = userRepository.findById(userDetails.getId());
        if (!foundUser.isPresent()) {
            throw new IllegalArgumentException("User not found in database with ID: " + userDetails.getId());
        }
        dataBaseUser = foundUser.get();

        Optional<UserLocation> existingBinding = userLocationRepository.findByUser(dataBaseUser);
        if (existingBinding.isPresent()) {
            throw new UserLocationDataAlreadyExistsException(
                    "UserLocation already exists with id: " + existingBinding.get().getId());
        }

        Optional<Location> foundLocation = locationRepository.findExistingLocation(
                location.getPoint().getLongitude(),
                location.getPoint().getLatitude(),
                1000);

        if (foundLocation.isPresent()) {
            boolean alreadyBound = userLocationRepository.existsByUserAndLocation(dataBaseUser,
                    foundLocation.get());
            if (alreadyBound) {
                throw new UserLocationDataAlreadyExistsException("User: " + dataBaseUser.getUsername()
                        + " is already saved to the same location with id: " + foundLocation.get().getId());
            }
            if (newBinding.getUser() == null) {
                newBinding.setUser(dataBaseUser);
            }
            newBinding.setLocation(foundLocation.get());
            userLocationRepository.save(newBinding);
            dataBaseUser.setLocation(newBinding);
            userRepository.save(dataBaseUser);

            return userLocationMapper.toDTO(newBinding);
        } else {
            Location createdLocation = createLocation(location);
            if (newBinding.getUser() == null) {
                newBinding.setUser(dataBaseUser);
            }
            newBinding.setLocation(createdLocation);
            userLocationRepository.save(newBinding);
            dataBaseUser.setLocation(newBinding);
            userRepository.save(dataBaseUser);
            return userLocationMapper.toDTO(newBinding);
        }
    }

    @Transactional
    public UserLocationDTO updateUserLocation(LocationDTO location, UserDetailsImpl userDetails) {
        User dataBaseUser = new User();
        UserLocation newBinding = new UserLocation();

        Optional<User> foundUser = userRepository.findById(userDetails.getId());
        if (!foundUser.isPresent()) {
            throw new IllegalArgumentException("User not found in database with ID: " + userDetails.getId());
        }
        dataBaseUser = foundUser.get();

        Optional<UserLocation> existingBinding = userLocationRepository.findByUser(dataBaseUser);
        if (existingBinding.isPresent()) {
            newBinding = existingBinding.get();
        } else {
            return createUserLocation(location, userDetails);
        }
        Location oldLocation = newBinding.getLocation();

        Optional<Location> foundLocation = locationRepository.findExistingLocation(
                location.getPoint().getLongitude(),
                location.getPoint().getLatitude(),
                1000);

        if (foundLocation.isPresent()) {
            if (!newBinding.getLocation().equals(foundLocation.get())) {
                newBinding.setLocation(foundLocation.get());
            }
        } else {
            newBinding.setLocation(createLocation(location));
        }

        userLocationRepository.save(newBinding);
        dataBaseUser.setLocation(newBinding);
        userRepository.save(dataBaseUser);

        if (oldLocation != null && !userLocationRepository.existsByLocation(oldLocation)) {
            locationRepository.delete(oldLocation);
            locationRepository.flush();
        }
        return userLocationMapper.toDTO(newBinding);
    }

    public void createMockUserLocation(LocationDTO location, String email) {
        Optional<Location> foundLocation = locationRepository.findExistingLocation(
                location.getPoint().getLongitude(),
                location.getPoint().getLatitude(),
                1000);
        Optional<User> foundUser = userRepository.findByEmail(email);

        User dataBaseUser = new User();
        if (foundUser.isPresent()) {
            dataBaseUser = foundUser.get();
        }

        if (foundLocation.isPresent()) {
            boolean alreadyBound = userLocationRepository.existsByUserAndLocation(dataBaseUser,
                    foundLocation.get());
            if (alreadyBound) {
                throw new UserLocationDataAlreadyExistsException("User: " + dataBaseUser.getUsername()
                        + " is already saved to the location with id: " + foundLocation.get().getId());
            }
            UserLocation newBinding = new UserLocation();
            newBinding.setUser(dataBaseUser);
            newBinding.setLocation(foundLocation.get());
            userLocationRepository.save(newBinding);
            dataBaseUser.setLocation(newBinding);
            userRepository.save(dataBaseUser);
        } else {
            Location createdLocation = createLocation(location);
            boolean alreadyBound = userLocationRepository.existsByUserAndLocation(dataBaseUser, createdLocation);
            if (alreadyBound) {
                throw new UserLocationDataAlreadyExistsException("User: " + dataBaseUser.getUsername()
                        + " is already saved to the location with id: " + foundLocation.get().getId());
            }
            UserLocation newBinding = new UserLocation();
            newBinding.setUser(dataBaseUser);
            newBinding.setLocation(createdLocation);
            userLocationRepository.save(newBinding);
            dataBaseUser.setLocation(newBinding);
            userRepository.save(dataBaseUser);
        }
    }

    public Set<LocationDTO> checkLocations(Set<LocationDTO> incomingLocations) {
        Set<LocationDTO> newLocations = new HashSet<>();
        for (LocationDTO location : incomingLocations) {
            Optional<Location> foundLocation = locationRepository.findExistingLocation(
                    location.getPoint().getLongitude(),
                    location.getPoint().getLatitude(),
                    1000);
            if (!foundLocation.isPresent()) {
                newLocations.add(location);
            }
        }
        return newLocations;

    }

    // Retrieve
    public LocationDTO getLocationById(Long id) {
        Optional<Location> existingLocation = locationRepository.findById(id);
        if (existingLocation.isPresent()) {
            LocationDTO foundLocationDTO = LocationMapper.toDTO(existingLocation.get());
            return foundLocationDTO;
        }
        throw new BadRequestException("Location with the id: " + id + " not found.");
    }

    public UserLocation getUserLocationByUserId(Long id) {
        User user = new User();
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            user = existingUser.get();
        }
        Optional<UserLocation> existingLocation = userLocationRepository
                .findByUser(user);
        if (existingLocation.isPresent()) {
            UserLocation foundLocation = existingLocation.get();
            return foundLocation;
        }
        throw new BadRequestException("UserLocation with the id: " + id + " not found.");
    }

    // Update

    @Transactional
    public LocationDTO updateLocation(LocationDTO incomingLocation) {
        if (incomingLocation.getId() != null) {
            Optional<Location> existingLocation = locationRepository.findById(incomingLocation.getId());
            if (existingLocation.isPresent()) {
                Location updatedLocation = existingLocation.get();

                LocationPoint coords = incomingLocation.getPoint();
                Coordinate coordinate = new Coordinate(coords.getLongitude(), coords.getLatitude());
                Point point = new GeometryFactory().createPoint(coordinate);
                point.setSRID(4326);
                updatedLocation.setPoint(point);

                locationRepository.save(updatedLocation);
                return LocationMapper.toDTO(updatedLocation);
            } else {
                createLocation(incomingLocation);
                return incomingLocation;
            }
        } else {
            createLocation(incomingLocation);
            return incomingLocation;
        }
    }

    // Delete
    public void deleteLocationById(Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
        } else {
            throw new NotFoundException("No Location found with the id: " + id);
        }
    }

}
