package com.example.Student_Management_System.service;

import com.example.Student_Management_System.dto.StudentDto;
import com.example.Student_Management_System.dto.StudentPerformanceDto;

import java.util.List;

public interface StudentService {

    StudentDto createStudent(StudentDto dto);

    StudentDto updateStudent(String rollNo, StudentDto dto);

    void deleteStudent(String rollNo);

    StudentDto getStudentByRollNo(String rollNo);

    List<StudentDto> getAllStudents();

    StudentPerformanceDto getStudentPerformanceByRollNo(String rollNo);
}
