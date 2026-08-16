package com.consultease.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.consultease.app.model.User;
import com.consultease.app.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createOrUpdateAdmin("06-2526-003597", "Melvin", "Soldevilla", "mecu.soldevilla.sjc@phinmaed.com", "Admin123!");

        createOrUpdateTeacher("T-HIS007", "Henry James", "Bautista", "henry.bautista@sjc.edu.ph", "HIS 007 - Life and Works of Rizal");
        createOrUpdateTeacher("T-GEN003", "Mark Joshua", "Vidar", "mark.vidar@sjc.edu.ph", "GEN 003 - Science Technology and Society");
        createOrUpdateTeacher("T-PED032", "Rafael", "De Torres", "rafael.detorres@sjc.edu.ph", "PED 032 - Physical Activities Towards Health and Fitness");
        createOrUpdateTeacher("T-ITE300", "Angel Mae", "Galario", "angelmae.galario@sjc.edu.ph", "ITE 300 - Object Oriented Programming");
        createOrUpdateTeacher("T-ITE298", "Angelo Jeric", "Trias", "angelojeric.trias@sjc.edu.ph", "ITE 298 - Information Management");
        createOrUpdateTeacher("T-ITE292", "Sigfried", "Breton", "sigfried.breton@sjc.edu.ph", "ITE 292 - Networking 1");
        createOrUpdateTeacher("T-ITE031", "Mark Anthony", "Cezar", "markanthony.cezar@sjc.edu.ph", "ITE 031 - Data Structures and Algorithms");
        createOrUpdateTeacher("T-ITE083", "Renjun", "Orain", "renjun.orain@sjc.edu.ph", "ITE 083 - IT Project Management");
        createOrUpdateTeacher("T-SSP005", "Maeryll Joy", "Fidelson", "maerylljoy.fidelson@sjc.edu.ph", "SSP 005 - Student Success Program 1");

        createOrUpdateStudent("03-2223-012345", "Juan", "Cruz", "student.test.sjc@phinmaed.com", "BSIT");
    }

    private void createOrUpdateAdmin(String idNumber, String firstName, String lastName, String email, String rawPassword) {
        User admin = userRepository.findByIdNumber(idNumber)
                .orElseGet(() -> userRepository.findByEmail(email).orElseGet(User::new));
        
        if (admin.getId() == null) {
            admin.setEmail(email);
            admin.setIdNumber(idNumber);
        }
        
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setFullName("Dr. " + firstName + " " + lastName);
        admin.setPassword(passwordEncoder.encode(rawPassword)); // 👈 Naka-BCrypt na ngayon
        admin.setRole(User.Role.ADMIN);
        admin.setCourse("Administration");
        admin.setCampus("PHINMA Saint Jude College Manila");
        admin.setIsVerified(true);
        
        userRepository.save(admin);
    }

    private void createOrUpdateTeacher(String idNumber, String firstName, String lastName, String email, String course) {
        User teacher = userRepository.findByIdNumber(idNumber)
                .orElseGet(() -> userRepository.findByEmail(email).orElseGet(User::new));
        
        if (teacher.getId() == null) {
            teacher.setIdNumber(idNumber);
            teacher.setEmail(email);
        }
        
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setFullName(firstName + " " + lastName);
        teacher.setPassword(passwordEncoder.encode("Teacher123!"));
        teacher.setRole(User.Role.FACULTY);
        teacher.setCourse(course);
        teacher.setCampus("PHINMA Saint Jude College Manila");
        teacher.setIsVerified(true);
        
        userRepository.save(teacher);
    }

    private void createOrUpdateStudent(String idNumber, String firstName, String lastName, String email, String course) {
        User student = userRepository.findByIdNumber(idNumber)
                .orElseGet(() -> userRepository.findByEmail(email).orElseGet(User::new));

        if (student.getId() == null) {
            student.setEmail(email);
            student.setIdNumber(idNumber);
        }

        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setFullName(firstName + " " + lastName);
        student.setPassword(passwordEncoder.encode("Student123!")); // 👈 Naka-BCrypt na ngayon
        student.setRole(User.Role.STUDENT);
        student.setCourse(course);
        student.setCampus("PHINMA Saint Jude College Manila");
        student.setIsVerified(true);

        userRepository.save(student);
    }
}