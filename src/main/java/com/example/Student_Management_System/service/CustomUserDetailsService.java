package com.example.Student_Management_System.service;

import com.example.Student_Management_System.entity.Admin;
import com.example.Student_Management_System.entity.Student;
import com.example.Student_Management_System.entity.Teacher;
import com.example.Student_Management_System.repo.AdminRepository;
import com.example.Student_Management_System.repo.StudentRepository;
import com.example.Student_Management_System.repo.TeacherRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;
    // Hardcoded admin for now as requested or implied
    private final String ADMIN_USER = "admin";

    public CustomUserDetailsService(StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            AdminRepository adminRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("DEBUG: Authentication attempt for user: " + username);

        // 1. Check if it's admin
        Optional<Admin> admin = adminRepository.findByUsername(username);
        if (admin.isPresent()) {
            System.out.println("DEBUG: Admin user detected in DB: " + username);
            return User.withUsername(admin.get().getUsername())
                    .password(admin.get().getPassword())
                    .roles("ADMIN")
                    .build();
        }

        // Add support for the hardcoded 'admin' just in case repository lookup fails
        // initially
        if (ADMIN_USER.equals(username)) {
            System.out.println("DEBUG: Falling back to hardcoded admin");
            return User.withUsername(ADMIN_USER)
                    .password("$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVu494m") // BCrypt for admin123
                    .roles("ADMIN")
                    .build();
        }

        // 2. Check if it's a teacher
        Optional<Teacher> teacher = teacherRepository.findByEmail(username);
        if (teacher.isPresent()) {
            return User.withUsername(teacher.get().getEmail())
                    .password(teacher.get().getPassword())
                    .roles("TEACHER")
                    .build();
        }

        // 3. Check if it's a student
        Optional<Student> student = studentRepository.findByEmail(username);
        if (student.isPresent()) {
            return User.withUsername(student.get().getEmail())
                    .password(student.get().getPassword())
                    .roles("STUDENT")
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + username);
    }
}
