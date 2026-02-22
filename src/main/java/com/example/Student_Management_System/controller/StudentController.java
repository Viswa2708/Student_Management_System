package com.example.Student_Management_System.controller;

import com.example.Student_Management_System.dto.ExamResultDto;
import com.example.Student_Management_System.dto.StudentPerformanceDto;
import com.example.Student_Management_System.dto.SubjectDto;
import com.example.Student_Management_System.entity.Student;
import com.example.Student_Management_System.entity.Subject;
import com.example.Student_Management_System.exception.ResourceNotFoundException;
import com.example.Student_Management_System.repo.StudentRepository;
import com.example.Student_Management_System.service.ExamService;
import com.example.Student_Management_System.service.StudentService;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final ExamService examService;

    public StudentController(StudentService studentService,
            StudentRepository studentRepository,
            ExamService examService) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.examService = examService;
    }

    // View enrolled subjects
    @GetMapping("/{rollNo}/subjects")
    public List<SubjectDto> getEnrolledSubjects(@PathVariable("rollNo") @NotNull String rollNo) {
        Student student = studentRepository.findByRollNo(rollNo)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with rollNo " + rollNo));

        return student.getEnrollments().stream().map(e -> {
            Subject s = e.getSubject();
            SubjectDto dto = new SubjectDto();
            dto.setId(s.getId());
            dto.setSubjectName(s.getSubjectName());
            dto.setSubjectCode(s.getSubjectCode());
            dto.setAssignedTeacherId(s.getAssignedTeacher() != null ? s.getAssignedTeacher().getId() : null);
            return dto;
        }).toList();
    }

    // View overall performance
    @GetMapping("/{rollNo}/performance")
    public StudentPerformanceDto getPerformance(@PathVariable("rollNo") String rollNo) {
        return studentService.getStudentPerformanceByRollNo(rollNo);
    }

    // View exam results (cycle tests, internals, semester exams)
    @GetMapping("/{rollNo}/exam-results")
    public List<ExamResultDto> getExamResults(@PathVariable("rollNo") String rollNo) {
        return examService.getStudentExamResults(rollNo);
    }
}
