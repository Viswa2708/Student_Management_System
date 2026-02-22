package com.example.Student_Management_System.service.impl;

import com.example.Student_Management_System.dto.SubjectDto;
import com.example.Student_Management_System.dto.SubjectPerformanceDto;
import com.example.Student_Management_System.entity.Subject;
import com.example.Student_Management_System.entity.Teacher;
import com.example.Student_Management_System.exception.ResourceNotFoundException;
import com.example.Student_Management_System.repo.SubjectRepository;
import com.example.Student_Management_System.repo.TeacherRepository;
import com.example.Student_Management_System.repo.ExamRepository;
import com.example.Student_Management_System.repo.ExamResultRepository;
import com.example.Student_Management_System.service.SubjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository,
            TeacherRepository teacherRepository,
            ExamRepository examRepository,
            ExamResultRepository examResultRepository) {
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
    }

    @Override
    public SubjectDto createSubject(SubjectDto dto) {
        Subject subject = new Subject();
        subject.setSubjectName(dto.getSubjectName());
        subject.setSubjectCode(dto.getSubjectCode());
        subject.setYear(dto.getYear());

        if (dto.getAssignedTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(dto.getAssignedTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Teacher not found with id " + dto.getAssignedTeacherId()));
            subject.setAssignedTeacher(teacher);
        }

        Subject saved = subjectRepository.save(subject);
        return mapToDto(saved);
    }

    @Override
    public SubjectDto updateSubject(Long id, SubjectDto dto) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + id));
        subject.setSubjectName(dto.getSubjectName());
        subject.setSubjectCode(dto.getSubjectCode());
        subject.setYear(dto.getYear());

        if (dto.getAssignedTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(dto.getAssignedTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Teacher not found with id " + dto.getAssignedTeacherId()));
            subject.setAssignedTeacher(teacher);
        } else {
            subject.setAssignedTeacher(null);
        }

        Subject saved = subjectRepository.save(subject);
        return mapToDto(saved);
    }

    @Override
    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject not found with id " + id);
        }
        subjectRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectDto getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + id));
        return mapToDto(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectDto> getAllSubjects() {
        return subjectRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubjectDto assignTeacherToSubject(Long subjectId, Long teacherId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + subjectId));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id " + teacherId));
        subject.setAssignedTeacher(teacher);
        Subject saved = subjectRepository.save(subject);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectPerformanceDto> getAllSubjectPerformance() {
        List<Subject> subjects = subjectRepository.findAll();
        List<SubjectPerformanceDto> result = new ArrayList<>();

        for (Subject subject : subjects) {
            List<com.example.Student_Management_System.entity.Exam> exams = examRepository.findBySubject(subject);
            List<com.example.Student_Management_System.entity.ExamResult> results = examResultRepository
                    .findByExamIn(exams);

            List<Double> percentages = results.stream()
                    .map(r -> (r.getMarksObtained() * 100.0) / r.getExam().getMaxMarks())
                    .toList();

            SubjectPerformanceDto dto = new SubjectPerformanceDto();
            dto.setSubjectId(subject.getId());
            dto.setSubjectName(subject.getSubjectName());

            if (!percentages.isEmpty()) {
                double avgPct = percentages.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                long pass = percentages.stream().filter(p -> p >= 40).count();
                long distinction = percentages.stream().filter(p -> p >= 75).count();
                dto.setAverageMarks(avgPct);
                dto.setPassPercentage(pass * 100.0 / percentages.size());
                dto.setDistinctionCount(distinction);
            } else {
                dto.setAverageMarks(0.0);
                dto.setPassPercentage(0.0);
                dto.setDistinctionCount(0L);
            }
            result.add(dto);
        }
        return result;
    }

    private SubjectDto mapToDto(Subject subject) {
        SubjectDto dto = new SubjectDto();
        dto.setId(subject.getId());
        dto.setSubjectName(subject.getSubjectName());
        dto.setSubjectCode(subject.getSubjectCode());
        dto.setAssignedTeacherId(subject.getAssignedTeacher() != null ? subject.getAssignedTeacher().getId() : null);
        dto.setYear(subject.getYear());
        return dto;
    }
}
