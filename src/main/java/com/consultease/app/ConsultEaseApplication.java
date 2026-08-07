package com.consultease.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class ConsultEaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsultEaseApplication.class, args);
        System.out.println("\n-------------------------------------------------------");
        System.out.println("  ConsultEase Web System is Running!");
        System.out.println("  Open your browser and go to: http://localhost:8080");
        System.out.println("-------------------------------------------------------\n");
    }
}