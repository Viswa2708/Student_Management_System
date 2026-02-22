package com.example.Student_Management_System.service.impl;

import com.example.Student_Management_System.dto.StudentDto;
import com.example.Student_Management_System.dto.StudentPerformanceDto;
import com.example.Student_Management_System.entity.Enrollment;
import com.example.Student_Management_System.entity.Student;
import com.example.Student_Management_System.entity.Subject;
import com.example.Student_Management_System.exception.ResourceNotFoundException;
import com.example.Student_Management_System.repo.EnrollmentRepository;
import com.example.Student_Management_System.repo.StudentRepository;
import com.example.Student_Management_System.repo.SubjectRepository;
import com.example.Student_Management_System.repo.ExamResultRepository;
import com.example.Student_Management_System.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamResultRepository examResultRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            EnrollmentRepository enrollmentRepository,
            ExamResultRepository examResultRepository,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.examResultRepository = examResultRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public StudentDto createStudent(StudentDto dto) {
        Student student = new Student();
        mapToEntity(dto, student);
        Student saved = studentRepository.save(student);

        if (dto.getSubjectIds() != null) {
            for (Long subjectId : dto.getSubjectIds()) {
                Subject subject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + subjectId));

                // Only allow subjects from the same year
                if (subject.getYear().equals(saved.getYear())) {
                    // Guard against duplicate enrollment
                    if (enrollmentRepository.findByStudentAndSubject(saved, subject).isEmpty()) {
                        Enrollment e = new Enrollment();
                        e.setStudent(saved);
                        e.setSubject(subject);
                        enrollmentRepository.save(e);
                    }
                }
            }
        }

        // Automatic year-based enrollment
        syncYearBasedEnrollments(saved);

        // Re-fetch to return accurate DTO with all enrollments
        Student fresh = studentRepository.findByRollNo(saved.getRollNo())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found after creation"));
        return mapToDto(fresh);
    }

    @Override
    public StudentDto updateStudent(String rollNo, StudentDto dto) {
        Student student = studentRepository.findByRollNo(rollNo)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with rollNo " + rollNo));
        mapToEntity(dto, student);
        studentRepository.save(student);

        // Update Enrollments using repository directly (more reliable than modifying
        // the in-memory collection)
        if (dto.getSubjectIds() != null) {
            // Get current enrollments from DB
            List<Enrollment> currentEnrollments = enrollmentRepository.findByStudent(student);
            List<Long> newSubjectIds = dto.getSubjectIds();

            // Delete enrollments whose subject is no longer in the list
            for (Enrollment e : currentEnrollments) {
                if (!newSubjectIds.contains(e.getSubject().getId())) {
                    enrollmentRepository.delete(e);
                }
            }

            // Add enrollments for newly selected subjects
            List<Long> currentSubjectIds = currentEnrollments.stream()
                    .map(e -> e.getSubject().getId())
                    .collect(Collectors.toList());

            for (Long subjectId : newSubjectIds) {
                if (!currentSubjectIds.contains(subjectId)) {
                    Subject subject = subjectRepository.findById(subjectId)
                            .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + subjectId));

                    // Only allow subjects from the same year
                    if (subject.getYear().equals(student.getYear())) {
                        // Check there's no duplicate enrollment (due to syncYearBased or race)
                        if (enrollmentRepository.findByStudentAndSubject(student, subject).isEmpty()) {
                            Enrollment e = new Enrollment();
                            e.setStudent(student);
                            e.setSubject(subject);
                            enrollmentRepository.save(e);
                        }
                    }
                }
            }
            enrollmentRepository.flush();
        }

        // Automatic year-based enrollment (adds subjects for the student's year if not
        // already enrolled)
        syncYearBasedEnrollments(student);

        // Re-fetch fresh student to return accurate DTO with updated enrollments
        Student fresh = studentRepository.findByRollNo(rollNo)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with rollNo " + rollNo));
        return mapToDto(fresh);
    }

    @Override
    public void deleteStudent(String rollNo) {
        Student student = studentRepository.findByRollNo(rollNo)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with rollNo " + rollNo));
        // Delete exam results first to avoid FK constraint violation
        examResultRepository.deleteAll(examResultRepository.findByStudent(student));
        studentRepository.delete(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDto getStudentByRollNo(String rollNo) {
        Student student = studentRepository.findByRollNo(rollNo)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with rollNo " + rollNo));
        return mapToDto(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentPerformanceDto getStudentPerformanceByRollNo(String rollNo) {
        Student student = studentRepository.findByRollNo(rollNo)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with rollNo " + rollNo));

        List<com.example.Student_Management_System.entity.ExamResult> results = examResultRepository
                .findByStudent(student);

        List<Double> percentages = results.stream()
                .filter(r -> r.getMarksObtained() != null)
                .map(r -> (r.getMarksObtained() * 100.0) / r.getExam().getMaxMarks())
                .toList();

        if (percentages.isEmpty()) {
            StudentPerformanceDto dto = new StudentPerformanceDto();
            dto.setStudentId(student.getId());
            dto.setStudentRollNo(student.getRollNo());
            dto.setStudentName(student.getName());
            dto.setAverageMarks(0.0);
            dto.setPassPercentage(0.0);
            return dto;
        }

        double avgPct = percentages.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        long passCount = percentages.stream().filter(p -> p >= 40).count();
        double passPercentage = passCount * 100.0 / percentages.size();

        StudentPerformanceDto dto = new StudentPerformanceDto();
        dto.setStudentId(student.getId());
        dto.setStudentRollNo(student.getRollNo());
        dto.setStudentName(student.getName());
        dto.setAverageMarks(avgPct);
        dto.setPassPercentage(passPercentage);
        return dto;
    }

    private void mapToEntity(StudentDto dto, Student student) {
        student.setRollNo(dto.getRollNo());
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setDepartment(dto.getDepartment());
        student.setYear(dto.getYear());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            student.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
    }

    private StudentDto mapToDto(Student student) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setRollNo(student.getRollNo());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setDepartment(student.getDepartment());
        dto.setYear(student.getYear());
        // For security, do NOT set password in DTO that goes back to client
        dto.setSubjectIds(student.getEnrollments().stream()
                .map(e -> e.getSubject().getId())
                .collect(Collectors.toList()));
        return dto;
    }

    private void syncYearBasedEnrollments(Student student) {
        List<Subject> allYearSubjects = subjectRepository.findByYear(student.getYear());
        List<Enrollment> currentEnrollments = enrollmentRepository.findByStudent(student);

        // 1. Remove enrollments for subjects that are NOT in the student's current year
        for (Enrollment e : currentEnrollments) {
            if (!e.getSubject().getYear().equals(student.getYear())) {
                enrollmentRepository.delete(e);
            }
        }
        enrollmentRepository.flush();

        // 2. Add subjects for the student's current year if missing
        List<Long> currentSubjectIds = enrollmentRepository.findByStudent(student).stream()
                .map(e -> e.getSubject().getId())
                .collect(Collectors.toList());

        for (Subject subject : allYearSubjects) {
            if (!currentSubjectIds.contains(subject.getId())) {
                if (enrollmentRepository.findByStudentAndSubject(student, subject).isEmpty()) {
                    Enrollment e = new Enrollment();
                    e.setStudent(student);
                    e.setSubject(subject);
                    enrollmentRepository.save(e);
                }
            }
        }
    }
}
