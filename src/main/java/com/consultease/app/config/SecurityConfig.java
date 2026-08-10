package com.consultease.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // 1. PUBLIC ENDPOINTS
                .requestMatchers("/", "/home", "/login", "/register", "/otp-verify", "/css/**", "/images/**", "/js/**", "/uploads/**").permitAll()
                
                // 2. ROLE-BASED ENDPOINTS
                .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "SUPERADMIN", "ROLE_SUPERADMIN")
                .requestMatchers("/faculty/**").hasAnyAuthority("FACULTY", "ROLE_FACULTY", "TEACHER", "ROLE_TEACHER")
                .requestMatchers("/student/**").hasAnyAuthority("STUDENT", "ROLE_STUDENT", "USER", "ROLE_USER")
                
                // 3. CATCH-ALL
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email") 
                .passwordParameter("password")
                
                // DYNAMIC REDIRECT AFTER SUCCESSFUL LOGIN
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
                    
                    boolean isFaculty = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY") || a.getAuthority().equals("FACULTY"));

                    if (isAdmin) {
                        response.sendRedirect("/admin/dashboard");
                    } else if (isFaculty) {
                        response.sendRedirect("/faculty/dashboard");
                    } else {
                        // 🟢 REDIRECT DIRECTLY TO STUDENT DASHBOARD AFTER LOGIN
                        response.sendRedirect("/student/dashboard");
                    }
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/") // 🟢 UPDATED: Redirects to home page after log out
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
