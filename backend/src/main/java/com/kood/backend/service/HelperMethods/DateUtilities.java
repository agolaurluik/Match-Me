package com.kood.backend.service.HelperMethods;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;

@Component
public class DateUtilities {

    public static boolean validateBirthdate(Instant birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Birthdate is required.");
        }

        try {

            LocalDate convertedDate = convertToLocalDate(birthDate);
            System.out.println("Received Instant: " + birthDate);
            System.out.println("Converted LocalDate: " + convertToLocalDate(birthDate));

            if (convertedDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Birthdate cannot be in the future.");
            }

            if (convertedDate.getYear() <= 1900) {
                throw new IllegalArgumentException("Birthdate year must be after 1900.");
            }
            return true;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid birthdate format. Please use 'yyyy-MM-dd' (year-month-day) where the month cannot be more than 12 and the longest month has 31 days.");
        }
    }

    public static LocalDate convertToLocalDate(Instant birthDate) {
        LocalDate date = birthDate.atZone(ZoneId.systemDefault()).toLocalDate();
        return date;
    }

    public static LocalDateTime convertToLocalDateTime(Instant timestamp) {
        LocalDateTime date = timestamp.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return date;
    }

    public static String convertDateToString(LocalDate timestampDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = timestampDate.format(formatter);
        return date;
    }

    public static String convertDateTimeToString(LocalDateTime timestampDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String date = timestampDate.format(formatter);
        return date;
    }
}
