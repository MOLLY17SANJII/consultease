package com.consultease.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // 👈 KAILANGAN ITO

import com.consultease.app.model.User;
import com.consultease.app.repository.UserRepository;

@Service // 👈 ITO ANG KULANG KAYA NAG-ERROR SA TERMINAL!
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        if (user.getPassword() != null && user.getPassword().equals(currentPassword)) {
            user.setPassword(newPassword);
            userRepository.save(user);
            return true;
        }

        return false;
    }
}