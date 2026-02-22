package com.example.Student_Management_System.service;

import com.example.Student_Management_System.dto.TeacherDto;
import com.example.Student_Management_System.dto.TeacherPerformanceDto;

import java.util.List;

public interface TeacherService {

    TeacherDto createTeacher(TeacherDto dto);

    TeacherDto updateTeacher(Long id, TeacherDto dto);

    void deleteTeacher(Long id);

    TeacherDto getTeacherById(Long id);

    List<TeacherDto> getAllTeachers();

    List<TeacherPerformanceDto> getAllTeacherPerformance();
}
