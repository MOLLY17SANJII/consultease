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
        message.setFrom("melvincbl17@gmail.com");
        message.setTo(toEmail);
        message.setSubject("ConsultEase - OTP Verification Code");
        message.setText("Your OTP verification code is: " + otpCode);
        
        mailSender.send(message);
        System.out.println("✅ OTP Email sent to: " + toEmail);
    }

    public void sendConsultationNotification(String teacherEmail, String studentName, String date, String time, String purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("melvincbl17@gmail.com");
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

    // 📧 NEW: Method to confirm to the student that their booking was successfully submitted
    public void sendConsultationSubmittedEmail(String studentEmail, String studentName, String teacherName, String date, String time, String purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("melvincbl17@gmail.com");
            message.setTo(studentEmail);
            message.setSubject("ConsultEase - Consultation Request Submitted Successfully");
            message.setText("Hello " + studentName + ",\n\n" +
                            "Your consultation request has been successfully submitted and sent to " + teacherName + ".\n\n" +
                            "Request Details:\n" +
                            "- Date: " + date + "\n" +
                            "- Time: " + time + "\n" +
                            "- Purpose: " + purpose + "\n\n" +
                            "You will receive another email update once the teacher reviews your request.\n\n" +
                            "Best regards,\nConsultEase System");
            
            mailSender.send(message);
            System.out.println("✅ Consultation Submission Confirmation Email sent to student: " + studentEmail);
        } catch (Exception e) {
            System.err.println("❌ ERROR SENDING STUDENT SUBMISSION EMAIL: " + e.getMessage());
        }
    }

    public void sendConsultationStatusUpdateEmail(String studentEmail, String studentName, String teacherName, String status, String date, String time, String remarks) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("melvincbl17@gmail.com");
            message.setTo(studentEmail);
            message.setSubject("ConsultEase - Consultation Request " + status);
            message.setText("Hello " + studentName + ",\n\n" +
                            "Your consultation request with " + teacherName + " has been updated.\n\n" +
                            "- Status: " + status + "\n" +
                            "- Date: " + date + "\n" +
                            "- Time: " + time + "\n\n" +
                            "Teacher's Remarks / Instructions:\n" + (remarks != null && !remarks.trim().isEmpty() ? remarks : "No remarks provided.") + "\n\n" +
                            "Please log in to your ConsultEase dashboard to view more details.\n\n" +
                            "Best regards,\nConsultEase System");
            
            mailSender.send(message);
            System.out.println("✅ Consultation Status Update Email sent to student: " + studentEmail);
        } catch (Exception e) {
            System.err.println("❌ ERROR SENDING STUDENT STATUS UPDATE EMAIL: " + e.getMessage());
        }
    }
}