package com.example.Student_Management_System.repo;

import com.example.Student_Management_System.entity.Exam;
import com.example.Student_Management_System.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findBySubject(Subject subject);

    List<Exam> findByTargetYear(Integer targetYear);

    List<Exam> findByExamType(String examType);

    List<Exam> findBySubjectIn(List<Subject> subjects);
}
