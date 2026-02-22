package com.example.Student_Management_System.controller;

import com.example.Student_Management_System.dto.*;
import com.example.Student_Management_System.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final SubjectService subjectService;
    private final AnalyticsService analyticsService;
    private final ExamService examService;

    public AdminController(StudentService studentService,
            TeacherService teacherService,
            SubjectService subjectService,
            AnalyticsService analyticsService,
            ExamService examService) {
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.subjectService = subjectService;
        this.analyticsService = analyticsService;
        this.examService = examService;
    }

    // ==================== Student CRUD ====================

    @PostMapping("/students")
    public StudentDto createStudent(@Valid @RequestBody StudentDto dto) {
        return studentService.createStudent(dto);
    }

    @PutMapping("/students/{rollNo}")
    public StudentDto updateStudent(@PathVariable("rollNo") String rollNo, @Valid @RequestBody StudentDto dto) {
        return studentService.updateStudent(rollNo, dto);
    }

    @DeleteMapping("/students/{rollNo}")
    public void deleteStudent(@PathVariable("rollNo") String rollNo) {
        studentService.deleteStudent(rollNo);
    }

    @GetMapping("/students/{rollNo}")
    public StudentDto getStudent(@PathVariable("rollNo") String rollNo) {
        return studentService.getStudentByRollNo(rollNo);
    }

    @GetMapping("/students")
    public List<StudentDto> getAllStudents() {
        return studentService.getAllStudents();
    }

    // ==================== Teacher CRUD ====================

    @PostMapping("/teachers")
    public TeacherDto createTeacher(@Valid @RequestBody TeacherDto dto) {
        return teacherService.createTeacher(dto);
    }

    @PutMapping("/teachers/{id}")
    public TeacherDto updateTeacher(@PathVariable("id") Long id, @Valid @RequestBody TeacherDto dto) {
        return teacherService.updateTeacher(id, dto);
    }

    @DeleteMapping("/teachers/{id}")
    public void deleteTeacher(@PathVariable("id") Long id) {
        teacherService.deleteTeacher(id);
    }

    @GetMapping("/teachers/{id}")
    public TeacherDto getTeacher(@PathVariable("id") Long id) {
        return teacherService.getTeacherById(id);
    }

    @GetMapping("/teachers")
    public List<TeacherDto> getAllTeachers() {
        return teacherService.getAllTeachers();
    }

    // ==================== Subject CRUD ====================

    @PostMapping("/subjects")
    public SubjectDto createSubject(@Valid @RequestBody SubjectDto dto) {
        return subjectService.createSubject(dto);
    }

    @PutMapping("/subjects/{id}")
    public SubjectDto updateSubject(@PathVariable("id") Long id, @Valid @RequestBody SubjectDto dto) {
        return subjectService.updateSubject(id, dto);
    }

    @DeleteMapping("/subjects/{id}")
    public void deleteSubject(@PathVariable("id") Long id) {
        subjectService.deleteSubject(id);
    }

    @GetMapping("/subjects/{id}")
    public SubjectDto getSubject(@PathVariable("id") Long id) {
        return subjectService.getSubjectById(id);
    }

    @GetMapping("/subjects")
    public List<SubjectDto> getAllSubjects() {
        return subjectService.getAllSubjects();
    }

    @PutMapping("/subjects/{subjectId}/assign-teacher/{teacherId}")
    public SubjectDto assignTeacher(@PathVariable("subjectId") Long subjectId,
            @PathVariable("teacherId") Long teacherId) {
        return subjectService.assignTeacherToSubject(subjectId, teacherId);
    }

    // ==================== Analytics ====================

    @GetMapping("/analytics/subjects")
    public List<SubjectPerformanceDto> getSubjectPerformance() {
        return analyticsService.getSubjectWiseAverages();
    }

    @GetMapping("/analytics/teachers")
    public List<TeacherPerformanceDto> getTeacherPerformance() {
        return analyticsService.getTeacherWisePerformance();
    }

    @GetMapping("/analytics/top-students")
    public List<StudentPerformanceDto> getTopStudents(@RequestParam(name = "limit", defaultValue = "100") int limit) {
        return analyticsService.getTopPerformingStudents(limit);
    }

    // ==================== Exam Scheduling ====================

    @PostMapping("/exams")
    public ExamDto createExam(@Valid @RequestBody ExamDto dto) {
        return examService.createExam(dto);
    }

    @PutMapping("/exams/{id}")
    public ExamDto updateExam(@PathVariable("id") Long id, @Valid @RequestBody ExamDto dto) {
        return examService.updateExam(id, dto);
    }

    @DeleteMapping("/exams/{id}")
    public void deleteExam(@PathVariable("id") Long id) {
        examService.deleteExam(id);
    }

    @GetMapping("/exams/{id}")
    public ExamDto getExam(@PathVariable("id") Long id) {
        return examService.getExamById(id);
    }

    @GetMapping("/exams")
    public List<ExamDto> getAllExams() {
        return examService.getAllExams();
    }

    @GetMapping("/exams/{id}/students")
    public List<ExamResultDto> getExamStudents(@PathVariable("id") Long id) {
        return examService.getExamStudents(id);
    }
}
