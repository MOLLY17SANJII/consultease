package com.consultease.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "faculty_schedules")
public class FacultySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    private User faculty;

    @Column(nullable = false)
    private String dayOfWeek; // e.g. "Mon / Wed / Fri"

    @Column(nullable = false)
    private String timeSlot; // e.g. "09:00 AM - 12:00 PM"

    @Column(nullable = false)
    private String officeLocation; // e.g. "Main Bldg - Room 302"

    public FacultySchedule() {}

    public FacultySchedule(User faculty, String dayOfWeek, String timeSlot, String officeLocation) {
        this.faculty = faculty;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.officeLocation = officeLocation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getFaculty() { return faculty; }
    public void setFaculty(User faculty) { this.faculty = faculty; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getOfficeLocation() { return officeLocation; }
    public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }
}