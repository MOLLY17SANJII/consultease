package com.consultease.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
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

    public void sendConsultationNotification(String teacherEmail, String studentName, String date, String time, String purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("mecu.soldevilla.sjc@gmail.com");
            message.setTo(teacherEmail);
            message.setSubject("New Consultation Request - ConsultEase");
            message.setText("Hello,\n\nYou have received a new consultation request from student: " + studentName + 
                            "\n\nDetails:" +
                            "\n- Date: " + date + 
                            "\n- Time: " + time + 
                            "\n- Purpose: " + purpose + 
                            "\n\nPlease log in to your ConsultEase dashboard to review and approve or decline this request.\n\nBest regards,\nConsultEase System");
            
            mailSender.send(message);
            System.out.println("✅ Consultation Notification Email sent to teacher: " + teacherEmail);
        } catch (Exception e) {
            System.err.println("❌ ERROR SENDING TEACHER NOTIFICATION EMAIL: " + e.getMessage());
        }
    }
}