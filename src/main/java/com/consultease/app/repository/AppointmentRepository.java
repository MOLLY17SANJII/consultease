package com.consultease.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.consultease.app.model.Appointment;
import com.consultease.app.model.User;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByStudent(User student);
    List<Appointment> findByProvider(User provider);
}