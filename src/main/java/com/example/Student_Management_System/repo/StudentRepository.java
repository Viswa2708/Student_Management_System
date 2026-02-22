package com.example.Student_Management_System.repo;

import com.example.Student_Management_System.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);

    Optional<Student> findByRollNo(String rollNo);
}
