package com.consultease.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.consultease.app.model.Consultation;
import com.consultease.app.model.User;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    // Student-specific queries
    List<Consultation> findByUserOrderByIdDesc(User user);
    long countByUser(User user);
    long countByUserAndStatus(User user, String status);

    // Administrative / Global queries (Super Admin)
    List<Consultation> findAllByOrderByIdDesc();
    List<Consultation> findByStatusOrderByIdDesc(String status);
    long countByStatus(String status);

    // Faculty-specific queries (Filtered by teacher name in targetHead)
    @Query("SELECT c FROM Consultation c WHERE LOWER(c.targetHead) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.id DESC")
    List<Consultation> findByTargetHeadContainingOrderByIdDesc(@Param("keyword") String keyword);

    @Query("SELECT c FROM Consultation c WHERE LOWER(c.targetHead) LIKE LOWER(CONCAT('%', :keyword, '%')) AND UPPER(c.status) = UPPER(:status) ORDER BY c.id DESC")
    List<Consultation> findByTargetHeadContainingAndStatusOrderByIdDesc(@Param("keyword") String keyword, @Param("status") String status);

    @Query("SELECT COUNT(c) FROM Consultation c WHERE LOWER(c.targetHead) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    long countByTargetHeadContaining(@Param("keyword") String keyword);

    @Query("SELECT COUNT(c) FROM Consultation c WHERE LOWER(c.targetHead) LIKE LOWER(CONCAT('%', :keyword, '%')) AND UPPER(c.status) = UPPER(:status)")
    long countByTargetHeadContainingAndStatus(@Param("keyword") String keyword, @Param("status") String status);
}