package com.example.Student_Management_System.dto;

import jakarta.validation.constraints.NotBlank;

public class SubjectDto {

    private Long id;

    @NotBlank
    private String subjectName;

    @NotBlank
    private String subjectCode;

    private Long assignedTeacherId;

    @jakarta.validation.constraints.NotNull
    private Integer year;

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

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public Long getAssignedTeacherId() {
        return assignedTeacherId;
    }

    public void setAssignedTeacherId(Long assignedTeacherId) {
        this.assignedTeacherId = assignedTeacherId;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
