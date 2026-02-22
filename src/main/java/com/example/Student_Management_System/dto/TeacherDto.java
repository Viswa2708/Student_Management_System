package com.example.Student_Management_System.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class TeacherDto {

    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    // Password is optional on update — leave blank to keep existing password
    private String password;

    private List<Long> subjectIds; // assigned subjects

    // analytics
    private Double averageMarks;
    private Double passPercentage;
    private Long distinctionCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Long> getSubjectIds() {
        return subjectIds;
    }

    public void setSubjectIds(List<Long> subjectIds) {
        this.subjectIds = subjectIds;
    }

    public Double getAverageMarks() {
        return averageMarks;
    }

    public void setAverageMarks(Double averageMarks) {
        this.averageMarks = averageMarks;
    }

    public Double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(Double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public Long getDistinctionCount() {
        return distinctionCount;
    }

    public void setDistinctionCount(Long distinctionCount) {
        this.distinctionCount = distinctionCount;
    }
}
