package com.consultease.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.consultease.app.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByIdNumber(String idNumber);
    List<User> findByRole(User.Role role);

    // Filter Query na gumagamit ng parameter para sa Role (Safe sa Hibernate 6)
    @Query("SELECT u FROM User u WHERE u.role = :role " +
           "AND (:dept IS NULL OR :dept = '' OR u.department = :dept) " +
           "AND (:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> filterFacultyWithRole(@Param("role") User.Role role, @Param("dept") String dept, @Param("search") String search);

    // Default method para hindi na kailangang baguhin ang tawag sa PageController
    default List<User> filterFaculty(String dept, String search) {
        return filterFacultyWithRole(User.Role.FACULTY, dept, search);
    }
}