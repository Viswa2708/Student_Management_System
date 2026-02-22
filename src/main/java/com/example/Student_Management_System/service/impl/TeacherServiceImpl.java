package com.example.Student_Management_System.service.impl;

import com.example.Student_Management_System.dto.TeacherDto;
import com.example.Student_Management_System.dto.TeacherPerformanceDto;
import com.example.Student_Management_System.entity.Exam;
import com.example.Student_Management_System.entity.ExamResult;
import com.example.Student_Management_System.entity.Subject;
import com.example.Student_Management_System.entity.Teacher;
import com.example.Student_Management_System.exception.ResourceNotFoundException;
import com.example.Student_Management_System.repo.ExamRepository;
import com.example.Student_Management_System.repo.ExamResultRepository;
import com.example.Student_Management_System.repo.SubjectRepository;
import com.example.Student_Management_System.repo.TeacherRepository;
import com.example.Student_Management_System.service.TeacherService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final SubjectRepository subjectRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherServiceImpl(TeacherRepository teacherRepository,
            ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            SubjectRepository subjectRepository,
            PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.subjectRepository = subjectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public TeacherDto createTeacher(TeacherDto dto) {
        Teacher teacher = new Teacher();
        teacher.setName(dto.getName());
        teacher.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            teacher.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        Teacher saved = teacherRepository.save(teacher);
        return mapToDto(saved);
    }

    @Override
    public TeacherDto updateTeacher(Long id, TeacherDto dto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id " + id));
        teacher.setName(dto.getName());
        teacher.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            teacher.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        Teacher saved = teacherRepository.save(teacher);
        return mapToDto(saved);
    }

    @Override
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id " + id));

        // Unassign teacher from all subjects to avoid FK constraint violation
        List<Subject> assignedSubjects = teacher.getSubjects();
        for (Subject subject : assignedSubjects) {
            subject.setAssignedTeacher(null);
            subjectRepository.save(subject);
        }
        // Flush the unassignment before deletion
        subjectRepository.flush();

        teacherRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherDto getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id " + id));
        return mapToDto(teacher);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherDto> getAllTeachers() {
        return teacherRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherPerformanceDto> getAllTeacherPerformance() {
        List<Teacher> teachers = teacherRepository.findAll();
        List<TeacherPerformanceDto> result = new ArrayList<>();

        for (Teacher teacher : teachers) {
            List<Subject> subjects = teacher.getSubjects();
            List<Exam> exams = examRepository.findBySubjectIn(subjects);
            List<ExamResult> results = examResultRepository.findByExamIn(exams);

            List<Double> percentages = results.stream()
                    .filter(r -> r.getMarksObtained() != null)
                    .map(r -> (r.getMarksObtained() * 100.0) / r.getExam().getMaxMarks())
                    .toList();

            TeacherPerformanceDto dto = new TeacherPerformanceDto();
            dto.setTeacherId(teacher.getId());
            dto.setTeacherName(teacher.getName());

            if (!percentages.isEmpty()) {
                double avgPct = percentages.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                long pass = percentages.stream().filter(p -> p >= 40).count();
                long distinction = percentages.stream().filter(p -> p >= 75).count();
                double passPct = pass * 100.0 / percentages.size();

                dto.setAverageMarks(avgPct);
                dto.setPassPercentage(passPct);
                dto.setDistinctionCount(distinction);
                dto.setPerformanceScore(
                        avgPct * 0.6 + passPct * 0.3 + (distinction * 100.0 / percentages.size()) * 0.1);
            } else {
                dto.setAverageMarks(0.0);
                dto.setPassPercentage(0.0);
                dto.setDistinctionCount(0L);
                dto.setPerformanceScore(0.0);
            }
            result.add(dto);
        }
        return result;
    }

    private TeacherDto mapToDto(Teacher teacher) {
        TeacherDto dto = new TeacherDto();
        dto.setId(teacher.getId());
        dto.setName(teacher.getName());
        dto.setEmail(teacher.getEmail());
        if (teacher.getSubjects() != null) {
            dto.setSubjectIds(teacher.getSubjects().stream()
                    .map(Subject::getId)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
