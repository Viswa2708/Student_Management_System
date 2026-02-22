package com.example.Student_Management_System.service.impl;

import com.example.Student_Management_System.dto.ExamDto;
import com.example.Student_Management_System.dto.ExamResultDto;
import com.example.Student_Management_System.entity.*;
import com.example.Student_Management_System.exception.ResourceNotFoundException;
import com.example.Student_Management_System.repo.*;
import com.example.Student_Management_System.service.ExamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final EnrollmentRepository enrollmentRepository;

    public ExamServiceImpl(ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            SubjectRepository subjectRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            EnrollmentRepository enrollmentRepository) {
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public ExamDto createExam(ExamDto dto) {
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + dto.getSubjectId()));

        Exam exam = new Exam();
        exam.setExamName(dto.getExamName());
        exam.setExamType(dto.getExamType());
        exam.setSubject(subject);
        exam.setTargetYear(dto.getTargetYear());
        exam.setMaxMarks(dto.getMaxMarks());
        exam.setDescription(dto.getDescription());

        if (dto.getExamDate() != null && !dto.getExamDate().isEmpty()) {
            exam.setExamDate(LocalDate.parse(dto.getExamDate()));
        }

        Exam saved = examRepository.save(exam);
        return mapToDto(saved);
    }

    @Override
    public ExamDto updateExam(Long id, ExamDto dto) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id " + id));

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + dto.getSubjectId()));

        exam.setExamName(dto.getExamName());
        exam.setExamType(dto.getExamType());
        exam.setSubject(subject);
        exam.setTargetYear(dto.getTargetYear());
        exam.setMaxMarks(dto.getMaxMarks());
        exam.setDescription(dto.getDescription());

        if (dto.getExamDate() != null && !dto.getExamDate().isEmpty()) {
            exam.setExamDate(LocalDate.parse(dto.getExamDate()));
        }

        Exam saved = examRepository.save(exam);
        return mapToDto(saved);
    }

    @Override
    public void deleteExam(Long id) {
        if (!examRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exam not found with id " + id);
        }
        examRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamDto getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id " + id));
        return mapToDto(exam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamDto> getAllExams() {
        return examRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamDto> getExamsByTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id " + teacherId));

        List<Subject> subjects = teacher.getSubjects();
        if (subjects.isEmpty()) {
            return new ArrayList<>();
        }

        return examRepository.findBySubjectIn(subjects)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResultDto> getExamStudents(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id " + examId));

        // Get only students enrolled in this specific subject
        List<Student> targetStudents = enrollmentRepository.findBySubject(exam.getSubject())
                .stream()
                .map(Enrollment::getStudent)
                .toList();

        // Map to DTOs including existing results
        return targetStudents.stream().map(student -> {
            ExamResultDto dto = new ExamResultDto();
            dto.setExamId(examId);
            dto.setStudentId(student.getId());
            dto.setStudentRollNo(student.getRollNo());
            dto.setStudentName(student.getName());
            dto.setStudentEmail(student.getEmail());
            dto.setMaxMarks(exam.getMaxMarks());
            dto.setExamName(exam.getExamName());
            dto.setSubjectName(exam.getSubject().getSubjectName());
            dto.setExamType(exam.getExamType());

            // Check if result already exists
            examResultRepository.findByExamAndStudent(exam, student)
                    .ifPresent(result -> {
                        dto.setId(result.getId());
                        dto.setMarksObtained(result.getMarksObtained());
                    });

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void addOrUpdateResult(ExamResultDto dto) {
        Exam exam = examRepository.findById(dto.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id " + dto.getExamId()));

        Student student;
        if (dto.getStudentRollNo() != null && !dto.getStudentRollNo().isBlank()) {
            student = studentRepository.findByRollNo(dto.getStudentRollNo())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Student not found with rollNo " + dto.getStudentRollNo()));
        } else {
            student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Student not found with id " + dto.getStudentId()));
        }

        // Validate marks don't exceed max
        if (dto.getMarksObtained() > exam.getMaxMarks()) {
            throw new IllegalArgumentException(
                    "Marks obtained (" + dto.getMarksObtained() + ") cannot exceed max marks (" + exam.getMaxMarks()
                            + ")");
        }

        ExamResult result = examResultRepository.findByExamAndStudent(exam, student)
                .orElse(new ExamResult());

        result.setExam(exam);
        result.setStudent(student);
        result.setMarksObtained(dto.getMarksObtained());

        examResultRepository.save(result);
    }

    @Override
    public void addOrUpdateBulkResults(List<ExamResultDto> dtos) {
        for (ExamResultDto dto : dtos) {
            addOrUpdateResult(dto);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResultDto> getStudentExamResults(String rollNo) {
        Student student = studentRepository.findByRollNo(rollNo)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with rollNo " + rollNo));

        return examResultRepository.findByStudent(student)
                .stream()
                .map(result -> {
                    ExamResultDto dto = new ExamResultDto();
                    dto.setId(result.getId());
                    dto.setExamId(result.getExam().getId());
                    dto.setStudentId(student.getId());
                    dto.setStudentRollNo(student.getRollNo());
                    dto.setStudentName(student.getName());
                    dto.setMarksObtained(result.getMarksObtained());
                    dto.setMaxMarks(result.getExam().getMaxMarks());
                    dto.setExamName(result.getExam().getExamName());
                    dto.setSubjectName(result.getExam().getSubject().getSubjectName());
                    dto.setSubjectCode(result.getExam().getSubject().getSubjectCode());
                    dto.setExamType(result.getExam().getExamType());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private ExamDto mapToDto(Exam exam) {
        ExamDto dto = new ExamDto();
        dto.setId(exam.getId());
        dto.setExamName(exam.getExamName());
        dto.setExamType(exam.getExamType());
        dto.setSubjectId(exam.getSubject().getId());
        dto.setSubjectName(exam.getSubject().getSubjectName());
        dto.setSubjectCode(exam.getSubject().getSubjectCode());
        dto.setTargetYear(exam.getTargetYear());
        dto.setMaxMarks(exam.getMaxMarks());
        dto.setExamDate(exam.getExamDate() != null ? exam.getExamDate().toString() : null);
        dto.setDescription(exam.getDescription());

        if (exam.getSubject().getAssignedTeacher() != null) {
            dto.setAssignedTeacherName(exam.getSubject().getAssignedTeacher().getName());
            dto.setAssignedTeacherId(exam.getSubject().getAssignedTeacher().getId());
        }

        // Count only students enrolled in this specific subject
        List<com.example.Student_Management_System.entity.Enrollment> enrollments = enrollmentRepository
                .findBySubject(exam.getSubject());
        int totalStudents = enrollments.size();
        dto.setTotalStudents(totalStudents);

        // Count results entered only for enrolled students (avoids mismatch like
        // 3/1=300%)
        List<Student> enrolledStudents = enrollments.stream()
                .map(com.example.Student_Management_System.entity.Enrollment::getStudent)
                .collect(java.util.stream.Collectors.toList());
        long resultsEntered = examResultRepository.findByExam(exam).stream()
                .filter(r -> enrolledStudents.stream().anyMatch(s -> s.getId().equals(r.getStudent().getId())))
                .count();
        dto.setResultsEntered((int) resultsEntered);

        return dto;
    }
}
