package com.example.Student_Management_System.config;

import com.example.Student_Management_System.entity.Admin;
import com.example.Student_Management_System.repo.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Read credentials from environment variables (set these before deploying!)
            String adminUsername = System.getenv("ADMIN_USERNAME");
            String adminPassword = System.getenv("ADMIN_PASSWORD");

            if (adminUsername == null || adminUsername.isBlank()) {
                adminUsername = "admin";
            }
            if (adminPassword == null || adminPassword.isBlank()) {
                adminPassword = "Viswa@2025";
            }

            final String finalUsername = adminUsername;
            final String finalPassword = adminPassword;

            // Always upsert: update password if admin exists, create if not
            Admin admin = adminRepository.findByUsername(finalUsername)
                    .orElseGet(() -> {
                        Admin a = new Admin();
                        a.setUsername(finalUsername);
                        return a;
                    });

            admin.setPassword(passwordEncoder.encode(finalPassword));
            adminRepository.save(admin);
            System.out.println("✅ Admin account ready. Username: " + finalUsername);
        };
    }
}
