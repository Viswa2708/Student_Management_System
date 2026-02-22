package com.example.Student_Management_System.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExamDto {

    private Long id;

    @NotBlank
    private String examName;

    @NotBlank
    private String examType; // CYCLE_TEST, INTERNAL, SEMESTER

    @NotNull
    private Long subjectId;

    private String subjectName;
    private String subjectCode;

    @NotNull
    @Min(1)
    private Integer targetYear;

    @NotNull
    @Min(1)
    private Integer maxMarks;

    private String examDate; // yyyy-MM-dd format

    private String description;

    private String assignedTeacherName;
    private Long assignedTeacherId;

    private Integer totalStudents;
    private Integer resultsEntered;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
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

    public Integer getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(Integer targetYear) {
        this.targetYear = targetYear;
    }

    public Integer getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(Integer maxMarks) {
        this.maxMarks = maxMarks;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignedTeacherName() {
        return assignedTeacherName;
    }

    public void setAssignedTeacherName(String assignedTeacherName) {
        this.assignedTeacherName = assignedTeacherName;
    }

    public Long getAssignedTeacherId() {
        return assignedTeacherId;
    }

    public void setAssignedTeacherId(Long assignedTeacherId) {
        this.assignedTeacherId = assignedTeacherId;
    }

    public Integer getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Integer totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Integer getResultsEntered() {
        return resultsEntered;
    }

    public void setResultsEntered(Integer resultsEntered) {
        this.resultsEntered = resultsEntered;
    }
}
