package com.consultease.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.consultease.app.model.User;
import com.consultease.app.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SettingsController {

    @Autowired
    private UserService userService;

    @GetMapping("/settings")
    public String showSettingsPage(Model model) {
        return "settings";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            loggedInUser = (User) session.getAttribute("user");
        }

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session expired. Please log in again.");
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password and confirm password do not match!");
            return "redirect:/student/dashboard?error=passwordMismatch";
        }

        boolean isUpdated = userService.changePassword(loggedInUser.getId(), currentPassword, newPassword);

        if (isUpdated) {
            loggedInUser.setPassword(newPassword);
            session.setAttribute("loggedInUser", loggedInUser);

            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
            return "redirect:/student/dashboard?success=passwordChanged";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Incorrect current password!");
            return "redirect:/student/dashboard?error=invalidCurrentPassword";
        }
    }
}