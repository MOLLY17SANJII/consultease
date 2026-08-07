package com.consultease.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.consultease.app.service.EmailService;

@Controller
public class PageController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // MAPAPADALA ANG OTP SA INBOX PAGKACLICK NG VERIFY
    @PostMapping("/register")
    public String processRegistration(
            @RequestParam("email") String email,
            @RequestParam(value = "idNumber", required = false) String idNumber, // Isinama na rin para sa ID validation
            Model model,
            RedirectAttributes redirectAttributes) {

        // ---------------------------------------------------------------------
        // 1. SCHOOL EMAIL VALIDATION (Dapat format: xxxx.surname.sjc@phinmaed.com)
        // ---------------------------------------------------------------------
        String emailRegex = "^[A-Za-z0-9._%+-]+\\.[A-Za-z0-9.-]+\\.sjc@phinmaed\\.com$";

        if (email == null || !email.matches(emailRegex)) {
            System.err.println("❌ INVALID EMAIL FORMAT: " + email);
            model.addAttribute("errorMessage", "Please use your official school email (e.g. name.surname.sjc@phinmaed.com)");
            return "register"; // Ire-reload ang register.html kasama ang error message
        }

        // ---------------------------------------------------------------------
        // 2. ID NUMBER VALIDATION (Format: 00-0000-000000) - Optional check
        // ---------------------------------------------------------------------
        if (idNumber != null && !idNumber.trim().isEmpty()) {
            String idRegex = "^\\d{2}-\\d{4}-\\d{6}$";
            if (!idNumber.matches(idRegex)) {
                System.err.println("❌ INVALID ID NUMBER FORMAT: " + idNumber);
                model.addAttribute("errorMessage", "Invalid ID format. Must follow 00-0000-000000.");
                return "register";
            }
        }

        // ---------------------------------------------------------------------
        // 3. GENERATE OTP & SEND EMAIL
        // ---------------------------------------------------------------------
        String otpCode = String.valueOf((int)(Math.random() * 900000) + 100000);
        
        System.out.println("==========================================");
        System.out.println("GENERATED OTP FOR " + email + ": " + otpCode);
        System.out.println("==========================================");

        try {
            emailService.sendOtpEmail(email, otpCode);
        } catch (Exception e) {
            System.err.println("❌ ERROR SENDING EMAIL: " + e.getMessage());
            e.printStackTrace();
            
            // Kapag nag-fail ang pag-send ng email (e.g. App Password issue)
            model.addAttribute("errorMessage", "Failed to send OTP email. Please try again later.");
            return "register";
        }

        return "redirect:/otp-verify";
    }

    @GetMapping("/otp-verify")
    public String showOtpPage() {
        return "otp-verify";
    }

    @PostMapping("/otp-verify")
    public String processOtp(@RequestParam("otp") String otp) {
        System.out.println("Submitted OTP: " + otp);
        return "redirect:/login";
    }

    @GetMapping("/student/dashboard")
    public String showDashboard() {
        return "student-dashboard";
    }
}