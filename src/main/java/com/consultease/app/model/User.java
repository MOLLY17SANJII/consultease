package com.consultease.app.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    public enum Role {
        STUDENT,
        FACULTY,
        ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String fullName;

    private String campus; 
    private String course;       
    private String department;   
    
    @Column(unique = true)
    private String idNumber;
    
    private String password;
    
    // 👈 BAGONG ADDED FIELD: Para sa Profile Picture Path
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    private Role role; 
    
    private String otpCode;
    private Boolean isVerified = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public String getFormattedFullName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        StringBuilder sb = new StringBuilder();
        if (firstName != null) sb.append(firstName).append(" ");
        if (middleName != null && !middleName.isBlank()) sb.append(middleName).append(" ");
        if (lastName != null) sb.append(lastName);
        if (suffix != null && !suffix.isBlank()) sb.append(" ").append(suffix);
        return sb.toString().trim();
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // 👈 GETTER AT SETTER PARA SA PROFILE PICTURE
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}