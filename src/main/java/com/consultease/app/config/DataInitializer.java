package com.consultease.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.consultease.app.model.User;
import com.consultease.app.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Pre-create or update 9 Subject Teachers with UNIQUE emails for Testing
        createOrUpdateTeacher("T-HIS007", "Henry James", "Bautista", "henry.bautista@sjc.edu.ph", "HIS 007 - Life and Works of Rizal");
        createOrUpdateTeacher("T-GEN003", "Mark Joshua", "Vidar", "mark.vidar@sjc.edu.ph", "GEN 003 - Science Technology and Society");
        createOrUpdateTeacher("T-PED032", "Rafael", "De Torres", "rafael.detorres@sjc.edu.ph", "PED 032 - Physical Activities Towards Health and Fitness");
        createOrUpdateTeacher("T-ITE300", "Angel Mae", "Galario", "angelmae.galario@sjc.edu.ph", "ITE 300 - Object Oriented Programming");
        createOrUpdateTeacher("T-ITE298", "Angelo Jeric", "Trias", "angelojeric.trias@sjc.edu.ph", "ITE 298 - Information Management");
        createOrUpdateTeacher("T-ITE292", "Sigfried", "Breton", "sigfried.breton@sjc.edu.ph", "ITE 292 - Networking 1");
        createOrUpdateTeacher("T-ITE031", "Mark Anthony", "Cezar", "markanthony.cezar@sjc.edu.ph", "ITE 031 - Data Structures and Algorithms");
        createOrUpdateTeacher("T-ITE083", "Renjun", "Orain", "renjun.orain@sjc.edu.ph", "ITE 083 - IT Project Management");
        createOrUpdateTeacher("T-SSP005", "Maeryll Joy", "Fidelson", "maerylljoy.fidelson@sjc.edu.ph", "SSP 005 - Student Success Program 1");

        // 2. Default Test Student Account
        createOrUpdateStudent("03-2223-012345", "Juan", "Cruz", "student.test.sjc@phinmaed.com", "BSIT");
    }

    private void createOrUpdateTeacher(String idNumber, String firstName, String lastName, String email, String course) {
        User teacher = userRepository.findByIdNumber(idNumber).orElseGet(User::new);
        
        if (teacher.getId() == null) {
            teacher.setIdNumber(idNumber);
        }
        
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setFullName(firstName + " " + lastName);
        teacher.setEmail(email);
        teacher.setPassword("Teacher123!");
        teacher.setRole(User.Role.FACULTY);
        teacher.setCourse(course);
        teacher.setCampus("PHINMA Saint Jude College Manila");
        teacher.setIsVerified(true);
        
        userRepository.save(teacher);
        System.out.println("Seeded/Updated Teacher Account: " + idNumber + " (" + firstName + " " + lastName + ")");
    }

    private void createOrUpdateStudent(String idNumber, String firstName, String lastName, String email, String course) {
        User student = userRepository.findByEmail(email).orElseGet(User::new);

        if (student.getId() == null) {
            student.setEmail(email);
        }

        student.setIdNumber(idNumber);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setFullName(firstName + " " + lastName);
        student.setPassword("Student123!");
        student.setRole(User.Role.STUDENT);
        student.setCourse(course);
        student.setCampus("PHINMA Saint Jude College Manila");
        student.setIsVerified(true);

        userRepository.save(student);
        System.out.println("Seeded/Updated Student Account: " + email + " [Course: " + course + "]");
    }
}