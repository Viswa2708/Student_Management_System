package com.example.Student_Management_System.dto;

public class SubjectPerformanceDto {

    private Long subjectId;
    private String subjectName;
    private Double averageMarks;
    private Double passPercentage;
    private Long distinctionCount;

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
