package com.consultease.app.service;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    // Validates Student ID format: 00-0000-000000
    public boolean isValidStudentId(String studentId) {
        if (studentId == null) return false;
        return Pattern.matches("^\\d{2}-\\d{4}-\\d{6}$", studentId);
    }

    // Validates Email Format: 2-chars-first + 2-chars-middle + '.' + last-name + '.sjc@phinmaed.com'
    public boolean isValidPhinmaEmail(String email, String firstName, String middleName, String lastName) {
        if (email == null || firstName == null || middleName == null || lastName == null) {
            return false;
        }

        firstName = firstName.trim().toLowerCase();
        middleName = middleName.trim().toLowerCase();
        lastName = lastName.trim().toLowerCase();
        email = email.trim().toLowerCase();

        if (firstName.length() < 2 || middleName.length() < 2) {
            return false;
        }

        String prefix = firstName.substring(0, 2) + middleName.substring(0, 2);
        String expectedEmail = prefix + "." + lastName + ".sjc@phinmaed.com";

        return email.equals(expectedEmail);
    }
}