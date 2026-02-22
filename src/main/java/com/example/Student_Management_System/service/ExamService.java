package com.example.Student_Management_System.service;

import com.example.Student_Management_System.dto.ExamDto;
import com.example.Student_Management_System.dto.ExamResultDto;

import java.util.List;

public interface ExamService {

    // Admin operations
    ExamDto createExam(ExamDto dto);

    ExamDto updateExam(Long id, ExamDto dto);

    void deleteExam(Long id);

    ExamDto getExamById(Long id);

    List<ExamDto> getAllExams();

    // Teacher operations
    List<ExamDto> getExamsByTeacher(Long teacherId);

    List<ExamResultDto> getExamStudents(Long examId);

    void addOrUpdateResult(ExamResultDto dto);

    void addOrUpdateBulkResults(List<ExamResultDto> dtos);

    // Student operations
    List<ExamResultDto> getStudentExamResults(String rollNo);
}
