package com.consultease.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service; // <-- Sumpain 'to kapag nawala!

@Service // <-- SIGURADUHING MANDATORY ITO!
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("mecu.soldevilla.sjc@gmail.com");
        message.setTo(toEmail);
        message.setSubject("ConsultEase - OTP Verification Code");
        message.setText("Your OTP verification code is: " + otpCode);
        
        mailSender.send(message);
        System.out.println("✅ OTP Email sent to: " + toEmail);
    }
}