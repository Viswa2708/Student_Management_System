package com.example.Student_Management_System.repo;

import com.example.Student_Management_System.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findBySubjectCode(String subjectCode);

    java.util.List<Subject> findByYear(Integer year);
}
