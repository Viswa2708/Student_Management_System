package com.example.Student_Management_System.controller;

import com.example.Student_Management_System.dto.ExamDto;
import com.example.Student_Management_System.dto.ExamResultDto;
import com.example.Student_Management_System.dto.SubjectDto;
import com.example.Student_Management_System.dto.SubjectPerformanceDto;
import com.example.Student_Management_System.service.AnalyticsService;
import com.example.Student_Management_System.service.ExamService;
import com.example.Student_Management_System.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin
public class TeacherController {

    private final SubjectService subjectService;
    private final AnalyticsService analyticsService;
    private final ExamService examService;

    public TeacherController(SubjectService subjectService,
            AnalyticsService analyticsService,
            ExamService examService) {
        this.subjectService = subjectService;
        this.analyticsService = analyticsService;
        this.examService = examService;
    }

    // View assigned subjects (filtering by teacherId for now)
    @GetMapping("/{teacherId}/subjects")
    public List<SubjectDto> getAssignedSubjects(@PathVariable("teacherId") Long teacherId) {
        return subjectService.getAllSubjects()
                .stream()
                .filter(s -> teacherId.equals(s.getAssignedTeacherId()))
                .toList();
    }

    // View subject performance report
    @GetMapping("/{teacherId}/subjects/performance")
    public List<SubjectPerformanceDto> getSubjectPerformance(@PathVariable("teacherId") Long teacherId) {
        return analyticsService.getSubjectWiseAverages()
                .stream()
                .filter(sp -> {
                    // in a richer DTO you'd include teacherId directly; for now front-end can
                    // cross-filter
                    return true;
                })
                .toList();
    }

    // ==================== Exam Results ====================

    // Get exams assigned to this teacher's subjects
    @GetMapping("/{teacherId}/exams")
    public List<ExamDto> getTeacherExams(@PathVariable("teacherId") Long teacherId) {
        return examService.getExamsByTeacher(teacherId);
    }

    // Get students for a specific exam (to enter results)
    @GetMapping("/exams/{examId}/students")
    public List<ExamResultDto> getExamStudents(@PathVariable("examId") Long examId) {
        return examService.getExamStudents(examId);
    }

    // Add or update a single exam result
    @PostMapping("/exams/results")
    public void addOrUpdateResult(@Valid @RequestBody ExamResultDto dto) {
        examService.addOrUpdateResult(dto);
    }

    // Add or update bulk exam results
    @PostMapping("/exams/results/bulk")
    public void addOrUpdateBulkResults(@Valid @RequestBody List<ExamResultDto> dtos) {
        examService.addOrUpdateBulkResults(dtos);
    }
}
