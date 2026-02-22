package com.example.Student_Management_System.repo;

import com.example.Student_Management_System.entity.Enrollment;
import com.example.Student_Management_System.entity.Student;
import com.example.Student_Management_System.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudent(Student student);

    List<Enrollment> findBySubject(Subject subject);

    Optional<Enrollment> findByStudentAndSubject(Student student, Subject subject);
}
