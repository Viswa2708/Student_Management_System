package com.example.Student_Management_System.dto;

public class TeacherPerformanceDto {

    private Long teacherId;
    private String teacherName;
    private Double averageMarks;
    private Double passPercentage;
    private Long distinctionCount;
    private Double performanceScore; // combined metric

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
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

    public Double getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(Double performanceScore) {
        this.performanceScore = performanceScore;
    }
}
