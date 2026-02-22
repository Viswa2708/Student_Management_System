package com.example.Student_Management_System.repo;

import com.example.Student_Management_System.entity.Exam;
import com.example.Student_Management_System.entity.ExamResult;
import com.example.Student_Management_System.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    List<ExamResult> findByExam(Exam exam);

    List<ExamResult> findByStudent(Student student);

    Optional<ExamResult> findByExamAndStudent(Exam exam, Student student);

    List<ExamResult> findByExamIn(List<Exam> exams);
}
