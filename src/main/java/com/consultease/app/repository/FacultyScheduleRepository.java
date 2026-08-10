package com.consultease.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.consultease.app.model.FacultySchedule;
import com.consultease.app.model.User;

@Repository
public interface FacultyScheduleRepository extends JpaRepository<FacultySchedule, Long> {
    List<FacultySchedule> findByFaculty(User faculty);
    List<FacultySchedule> findAllByOrderByIdDesc();
}