package com.consultease.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.consultease.app.model.User;
import com.consultease.app.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("==================================================");
        System.out.println("🔍 LOGIN ATTEMPT RECEIVED FOR: [" + email + "]");

        // 1. Hanapin ang user sa database (naka-trim at lowercase para iwas sa typo)
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> {
                    System.out.println("❌ ERROR: Email [" + email + "] is NOT FOUND in database!");
                    System.out.println("==================================================");
                    return new UsernameNotFoundException("User not found with Email: " + email);
                });

        System.out.println("✅ USER FOUND IN DATABASE!");
        System.out.println("   -> ID: " + user.getId());
        System.out.println("   -> Email: " + user.getEmail());
        System.out.println("   -> Stored Password Hash: " + user.getPassword());
        System.out.println("   -> Verified Status: " + user.getIsVerified());

        String roleName = (user.getRole() != null) ? user.getRole().name() : "STUDENT";
        System.out.println("   -> Assigned Role: " + roleName);
        System.out.println("==================================================");

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(roleName)
                .build();
    }
}