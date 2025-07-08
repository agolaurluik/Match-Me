package com.kood.backend.service.HelperMethods;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;
import com.kood.backend.dto.LocationDTOs.LocationDTO;
import com.kood.backend.entity.Entities.Gender;
import com.kood.backend.entity.Entities.Interest;
import com.kood.backend.entity.Entities.Nationality;
import com.kood.backend.entity.Entities.Personality;
import com.kood.backend.entity.Entities.Purpose;
import com.kood.backend.entity.LocationEntities.Location;
import com.kood.backend.entity.UserEntities.MatchingFilter;
import com.kood.backend.entity.UserEntities.User;
import com.kood.backend.exceptions.DatabaseNotReadyException;
import com.kood.backend.mapper.LocationMapper;
import com.kood.backend.repository.GenderRepository;
import com.kood.backend.repository.InterestRepository;
import com.kood.backend.repository.NationalityRepository;
import com.kood.backend.repository.PersonalityRepository;
import com.kood.backend.repository.PurposeRepository;
import com.kood.backend.repository.UserRepository;
import com.kood.backend.service.InterestService;
import com.kood.backend.service.LocationService;
import com.kood.backend.service.PersonalityService;

import lombok.RequiredArgsConstructor;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class TestUserSeeder implements CommandLineRunner {

    private final LocationService locationService;
    private final InterestService interestService;
    private final PersonalityService personalityService; // This runs automatically when the app starts, so a 100 new
                                                         // random users on each startup
    private final UserRepository userRepository;
    private final GenderRepository genderRepository;
    private final NationalityRepository nationalityRepository;
    private final PersonalityRepository personalityRepository;
    private final InterestRepository interestRepository;
    private final PurposeRepository purposeRepository;

    private final Random random = new Random();
    private final Faker faker = new Faker();

    @Value("${mockUsers.skip:false}")
    private boolean skipMockUsers;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (skipMockUsers) {
            System.err
                    .println(
                            "--- Skipping Mock User data generation --- This is usually on by default");
            return;
        }

        long existingUserCount = userRepository.count();
        int usersToCreate = 100 - (int) existingUserCount;
        if (usersToCreate <= 0) {
            System.err.println("Database already has at least 100 users, skipping seeding");
            return;
        } else if (usersToCreate > 99) {
            long existingInterestCount = interestRepository.count();
            if (existingInterestCount < 5) {
                throw new DatabaseNotReadyException(
                        "Not enough user entity data available in the database to generate users, please generate all the pertaining fields before starting again or disable mock user generation");
            }
        }

        String defaultImageName = ("default-user.jpg");

        for (int i = 0; i < 100; i++) {
            User user = new User();
            user.setUsername(faker.name().username());
            user.setEmail(faker.internet().emailAddress());
            user.setBirthDate(getRandomBirthDate(LocalDate.now().getYear() - 115, LocalDate.now().getYear() - 18));
            user.setLastSeen(getRandomLastSeenDate(LocalDate.now().minusDays(30), LocalDate.now()));
            user.setGender(getRandomGender());
            user.setNationality(getRandomNationality());
            String rawPassword = "test";
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setProfileImageName(defaultImageName);
            user.setInterests(getRandomInterests());
            user.setPersonalities(getRandomPersonalities());
            user.setPurpose(getRandomPurpose());

            MatchingFilter filter = new MatchingFilter();
            filter.setUser(user);
            user.setMatchingFilter(filter);

            filter.setMatch_limit(9);
            filter.setGenderPreference("lookingforall");
            filter.setNationalityScore(1);
            filter.setInterestScore(1);
            filter.setPersonalityScore(1);
            filter.setPurposeScore(1);
            filter.setHighestAge(100);
            filter.setLowestAge(0);
            filter.setRadius(1000);

            userRepository.save(user);
            String locationFlag = "default";
            if (i >= 30 && i < 40) {
                locationFlag = "lithuania";
            } else if (i >= 40 && i < 50) {
                locationFlag = "latvia";
            } else if (i >= 50 && i < 60) {
                locationFlag = "poland";
            } else if (i >= 60 && i < 70) {
                locationFlag = "germany";
            } else if (i >= 70 && i < 100) {
                locationFlag = "estonia";
            }
            locationService.createMockUserLocation(getRandomLocation(locationFlag), user.getEmail());
        }
    }

    public LocationDTO getRandomLocation(String locationFlag) {
        double randomLatitude = 0;
        double randomLongitude = 0;
        double randomAccuracy = 5 + (95 * random.nextDouble());
        if (locationFlag.equals("estonia")) {
            randomLatitude = 57 + (60.5 - 57) * random.nextDouble();
            randomLongitude = 21 + (28 - 21) * random.nextDouble();
        } else if (locationFlag.equals("latvia")) {
            randomLatitude = 55 + (58 - 55) * random.nextDouble();
            randomLongitude = 21 + (28 - 21) * random.nextDouble();
        } else if (locationFlag.equals("lithuania")) {
            randomLatitude = 53 + (56 - 53) * random.nextDouble();
            randomLongitude = 21 + (26 - 21) * random.nextDouble();
        } else if (locationFlag.equals("germany")) {
            randomLatitude = 47 + (55 - 47) * random.nextDouble();
            randomLongitude = 5 + (15 - 5) * random.nextDouble();
        } else if (locationFlag.equals("poland")) {
            randomLatitude = 49 + (55 - 49) * random.nextDouble();
            randomLongitude = 14 + (24 - 14) * random.nextDouble();
        } else {
            randomLatitude = 35 + (70 - 35) * random.nextDouble();
            randomLongitude = -10 + (40 + 10) * random.nextDouble();
        }

        Instant now = Instant.now();
        long randomMinutes = (long) (random.nextDouble() * 30 * 24 * 60);
        // Random number of minutes between 0 and 30 days
        Instant randomTimestamp = now.minus(randomMinutes, ChronoUnit.MINUTES);

        Location randomLocationData = new Location();
        Coordinate coordinate = new Coordinate(randomLongitude, randomLatitude);
        Point randomPoint = new GeometryFactory().createPoint(coordinate);
        randomPoint.setSRID(4326);

        randomLocationData.setAccuracy(randomAccuracy);
        randomLocationData.setPoint(randomPoint);
        randomLocationData.setTimestamp(randomTimestamp);
        LocationDTO preparedLocation = LocationMapper.toDTO(randomLocationData);
        locationService.createLocation(preparedLocation);
        return preparedLocation;
    }

    public Instant getRandomBirthDate(int fromYear, int toYear) {
        LocalDate start = LocalDate.of(fromYear, 1, 1);
        LocalDate end = LocalDate.of(toYear, 12, 31);

        long startDay = start.toEpochDay(); // Convert to a number before or after January 1st 1970
        long endDay = end.toEpochDay();

        long randomDay = random.nextLong(startDay, endDay + 1); // Random number between these two + 1 to include the
                                                                // last day as well

        LocalDate randomDate = LocalDate.ofEpochDay(randomDay);
        return randomDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public Instant getRandomLastSeenDate(LocalDate fromDate, LocalDate toDate) {

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be before or equal to toDate");
        }

        long startDay = fromDate.toEpochDay();
        long endDay = toDate.toEpochDay();

        long randomDay = random.nextLong(startDay, endDay + 1);

        LocalDate randomDate = LocalDate.ofEpochDay(randomDay);

        int hour = random.nextInt(24);
        int minutes = random.nextInt(60);
        int seconds = random.nextInt(60);

        LocalTime time = LocalTime.of(hour, minutes, seconds);
        return time.atDate(randomDate).atZone(ZoneId.systemDefault()).toInstant();
    }

    public Set<Long> pickMultipleRandom(Long minID, Long maxID, int count) {

        Set<Long> randomIDSet = new HashSet<>();
        while (randomIDSet.size() < count) {
            Long randomNumber = random.nextLong(maxID - minID + 1) + minID;
            randomIDSet.add(randomNumber);
        }
        return randomIDSet;
    }

    public int getRandomCount(int minCount, int maxCount) {
        return random.nextInt(maxCount - minCount + 1) + minCount;
    }

    public Set<Interest> getRandomInterests() {
        int randomInterestCount = getRandomCount(2, 5);
        List<Interest> allInterests = interestRepository.findAll();
        Long minInterests = (long) 1;
        Long maxInterests = (long) allInterests.size();
        Set<Long> randomSetIntegers = pickMultipleRandom(minInterests, maxInterests, randomInterestCount);

        Set<Interest> aSetOfRandomInterests = new HashSet<>();
        for (Long randomID : randomSetIntegers) {
            Interest newInterest = interestService.getInterestById(randomID);
            if (newInterest != null) {
                aSetOfRandomInterests.add(newInterest);
            }
        }
        return aSetOfRandomInterests;
    }

    public Set<Personality> getRandomPersonalities() {
        int randomPersonalityCount = getRandomCount(2, 5);
        List<Personality> allPersonalities = personalityRepository.findAll();
        Long minPersonalities = (long) 2;
        Long maxPersonalities = (long) allPersonalities.size();
        Set<Long> randomSetIntegers = pickMultipleRandom(minPersonalities, maxPersonalities, randomPersonalityCount);

        Set<Personality> aSetOfRandomPersonalities = new HashSet<>();
        for (Long randomID : randomSetIntegers) {
            Personality newPersonality = personalityService.getPersonalityById(randomID);
            if (newPersonality != null) {
                aSetOfRandomPersonalities.add(newPersonality);
            }
        }
        return aSetOfRandomPersonalities;
    }

    public Purpose getRandomPurpose() {

        List<Purpose> allPurposes = purposeRepository.findAll();
        if (allPurposes.isEmpty()) {
            throw new DatabaseNotReadyException("Database is not ready = no <Purpose> available in database");
        }
        return allPurposes.get(random.nextInt(allPurposes.size()));
    }

    public Gender getRandomGender() {
        List<Gender> allGenders = genderRepository.findAll();
        if (allGenders.isEmpty()) {
            throw new DatabaseNotReadyException("Database is not ready = no <Gender> available in database");
        }
        return allGenders.get(random.nextInt(allGenders.size()));
    }

    public Nationality getRandomNationality() {
        List<Nationality> allNationalities = nationalityRepository.findAll();
        if (allNationalities.isEmpty()) {
            throw new DatabaseNotReadyException("Database is not ready = no <Nationality> available in database");
        }
        return allNationalities.get(random.nextInt(allNationalities.size()));
    }

}
