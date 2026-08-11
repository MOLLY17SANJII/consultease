package com.consultease.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            String subject = "ConsultEase - OTP Verification Code";
            String textContent = "Your OTP verification code is: " + otpCode;
            
            sendViaResendApi(toEmail, subject, textContent);
            System.out.println("✅ OTP Email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ ERROR SENDING OTP EMAIL: " + e.getMessage());
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    public void sendConsultationNotification(String teacherEmail, String studentName, String date, String time, String purpose) {
        try {
            String subject = "New Consultation Request - ConsultEase";
            String textContent = "Hello,\n\nYou have received a new consultation request from student: " + studentName + 
                                 "\n\nDetails:" +
                                 "\n- Date: " + date + 
                                 "\n- Time: " + time + 
                                 "\n- Purpose: " + purpose + 
                                 "\n\nPlease log in to your ConsultEase dashboard to review and approve or decline this request.\n\nBest regards,\nConsultEase System";
            
            sendViaResendApi(teacherEmail, subject, textContent);
            System.out.println("✅ Consultation Notification Email sent to teacher: " + teacherEmail);
        } catch (Exception e) {
            System.err.println("❌ ERROR SENDING TEACHER NOTIFICATION EMAIL: " + e.getMessage());
        }
    }

    private void sendViaResendApi(String to, String subject, String textContent) {
        // Alisin ang anumang hidden spaces o newlines sa API key para maiwasan ang header error
        String cleanApiKey = resendApiKey != null ? resendApiKey.trim() : "";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + cleanApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("from", "ConsultEase <onboarding@resend.dev>");
        body.put("to", new String[]{to});
        body.put("subject", subject);
        body.put("text", textContent);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(
            RESEND_API_URL,
            HttpMethod.POST,
            entity,
            String.class
        );
    }
}