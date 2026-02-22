package com.example.Student_Management_System.service;

import com.example.Student_Management_System.dto.SubjectDto;
import com.example.Student_Management_System.dto.SubjectPerformanceDto;

import java.util.List;

public interface SubjectService {

    SubjectDto createSubject(SubjectDto dto);

    SubjectDto updateSubject(Long id, SubjectDto dto);

    void deleteSubject(Long id);

    SubjectDto getSubjectById(Long id);

    List<SubjectDto> getAllSubjects();

    SubjectDto assignTeacherToSubject(Long subjectId, Long teacherId);

    List<SubjectPerformanceDto> getAllSubjectPerformance();
}
