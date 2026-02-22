package com.example.Student_Management_System.service;

import com.example.Student_Management_System.dto.*;

import java.util.List;

public interface AnalyticsService {

    List<SubjectPerformanceDto> getSubjectWiseAverages();

    List<TeacherPerformanceDto> getTeacherWisePerformance();

    List<StudentPerformanceDto> getTopPerformingStudents(int limit);

    List<StudentPerformanceDto> getTopStudentsByYear(int year, int limit);
}
