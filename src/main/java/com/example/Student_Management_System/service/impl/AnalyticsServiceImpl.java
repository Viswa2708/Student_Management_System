package com.example.Student_Management_System.service.impl;

import com.example.Student_Management_System.dto.*;
import com.example.Student_Management_System.entity.Student;
import com.example.Student_Management_System.entity.Subject;
import com.example.Student_Management_System.entity.Teacher;
import com.example.Student_Management_System.entity.Exam;
import com.example.Student_Management_System.entity.ExamResult;
import com.example.Student_Management_System.repo.StudentRepository;
import com.example.Student_Management_System.repo.SubjectRepository;
import com.example.Student_Management_System.repo.TeacherRepository;
import com.example.Student_Management_System.repo.ExamRepository;
import com.example.Student_Management_System.repo.ExamResultRepository;
import com.example.Student_Management_System.service.AnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;

    public AnalyticsServiceImpl(SubjectRepository subjectRepository,
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            ExamRepository examRepository,
            ExamResultRepository examResultRepository) {
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
    }

    @Override
    public List<SubjectPerformanceDto> getSubjectWiseAverages() {
        List<Subject> subjects = subjectRepository.findAll();
        List<SubjectPerformanceDto> result = new ArrayList<>();

        for (Subject subject : subjects) {
            List<com.example.Student_Management_System.entity.Exam> exams = examRepository.findBySubject(subject);
            List<com.example.Student_Management_System.entity.ExamResult> results = examResultRepository
                    .findByExamIn(exams);

            List<Double> percentages = results.stream()
                    .filter(r -> r.getMarksObtained() != null)
                    .map(r -> (r.getMarksObtained() * 100.0) / r.getExam().getMaxMarks())
                    .toList();

            SubjectPerformanceDto dto = new SubjectPerformanceDto();
            dto.setSubjectId(subject.getId());
            dto.setSubjectName(subject.getSubjectName());

            if (!percentages.isEmpty()) {
                double avgPct = percentages.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                long pass = percentages.stream().filter(p -> p >= 40).count();
                long distinction = percentages.stream().filter(p -> p >= 75).count();

                dto.setAverageMarks(avgPct); // Using average percentage as "Average Marks"
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

    @Override
    public List<TeacherPerformanceDto> getTeacherWisePerformance() {
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

    @Override
    public List<StudentPerformanceDto> getTopPerformingStudents(int limit) {
        List<Student> students = studentRepository.findAll();
        List<StudentPerformanceDto> perf = students.stream().map(student -> {
            List<com.example.Student_Management_System.entity.ExamResult> results = examResultRepository
                    .findByStudent(student);

            List<Double> percentages = results.stream()
                    .filter(r -> r.getMarksObtained() != null)
                    .map(r -> (r.getMarksObtained() * 100.0) / r.getExam().getMaxMarks())
                    .toList();

            StudentPerformanceDto dto = new StudentPerformanceDto();
            dto.setStudentId(student.getId());
            dto.setStudentRollNo(student.getRollNo());
            dto.setStudentName(student.getName());
            dto.setYear(student.getYear());
            if (!percentages.isEmpty()) {
                double avgPct = percentages.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                long pass = percentages.stream().filter(p -> p >= 40).count();
                double passPct = pass * 100.0 / percentages.size();
                dto.setAverageMarks(avgPct);
                dto.setPassPercentage(passPct);
            } else {
                dto.setAverageMarks(0.0);
                dto.setPassPercentage(0.0);
            }
            return dto;
        }).collect(Collectors.toList());

        return perf.stream()
                .sorted(Comparator.comparingDouble(StudentPerformanceDto::getAverageMarks).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public List<StudentPerformanceDto> getTopStudentsByYear(int year, int limit) {
        List<Student> students = studentRepository.findAll()
                .stream()
                .filter(s -> s.getYear() != null && s.getYear() == year)
                .toList();

        List<StudentPerformanceDto> perf = students.stream().map(student -> {
            List<com.example.Student_Management_System.entity.ExamResult> results = examResultRepository
                    .findByStudent(student);

            List<Double> percentages = results.stream()
                    .filter(r -> r.getMarksObtained() != null)
                    .map(r -> (r.getMarksObtained() * 100.0) / r.getExam().getMaxMarks())
                    .toList();

            StudentPerformanceDto dto = new StudentPerformanceDto();
            dto.setStudentId(student.getId());
            dto.setStudentRollNo(student.getRollNo());
            dto.setStudentName(student.getName());
            dto.setYear(student.getYear());
            if (!percentages.isEmpty()) {
                double avgPct = percentages.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                long pass = percentages.stream().filter(p -> p >= 40).count();
                double passPct = pass * 100.0 / percentages.size();
                dto.setAverageMarks(avgPct);
                dto.setPassPercentage(passPct);
            } else {
                dto.setAverageMarks(0.0);
                dto.setPassPercentage(0.0);
            }
            return dto;
        }).collect(Collectors.toList());

        return perf.stream()
                .sorted(Comparator.comparingDouble(StudentPerformanceDto::getAverageMarks).reversed())
                .limit(limit)
                .toList();
    }
}
