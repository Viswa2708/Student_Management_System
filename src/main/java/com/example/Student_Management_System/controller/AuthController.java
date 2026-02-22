package com.example.Student_Management_System.controller;

import com.example.Student_Management_System.entity.Teacher;
import com.example.Student_Management_System.repo.StudentRepository;
import com.example.Student_Management_System.repo.TeacherRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public AuthController(TeacherRepository teacherRepository, StudentRepository studentRepository) {
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("username", authentication.getName());

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5)) // Remove "ROLE_" prefix
                .findFirst()
                .orElse("USER");

        user.put("role", role);

        // If TEACHER role, include the teacher's DB ID and name
        if ("TEACHER".equals(role)) {
            Optional<Teacher> teacher = teacherRepository.findByEmail(authentication.getName());
            teacher.ifPresent(t -> {
                user.put("teacherId", t.getId());
                user.put("displayName", t.getName());
            });
        }

        // If STUDENT role, include the student's rollNo and name
        if ("STUDENT".equals(role)) {
            studentRepository.findByEmail(authentication.getName())
                    .ifPresent(s -> {
                        user.put("rollNo", s.getRollNo());
                        user.put("displayName", s.getName());
                    });
        }

        return user;
    }
}
