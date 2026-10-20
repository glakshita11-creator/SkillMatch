package com.skillmatch.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EligibilityCheckResponse {
    private boolean eligible;
    private List<String> reasons = new ArrayList<>();
    private BigDecimal studentCgpa;
    private BigDecimal requiredCgpa;
    private boolean cgpaSatisfied;
    private String studentDepartment;
    private String eligibleDepartments;
    private boolean departmentSatisfied;
    private Integer studentGraduationYear;
    private String eligibleGraduationYears;
    private boolean graduationYearSatisfied;
    private boolean deadlinePassed;
    private boolean alreadyApplied;
    private boolean profileIncomplete;

    public EligibilityCheckResponse() {
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public BigDecimal getStudentCgpa() {
        return studentCgpa;
    }

    public void setStudentCgpa(BigDecimal studentCgpa) {
        this.studentCgpa = studentCgpa;
    }

    public BigDecimal getRequiredCgpa() {
        return requiredCgpa;
    }

    public void setRequiredCgpa(BigDecimal requiredCgpa) {
        this.requiredCgpa = requiredCgpa;
    }

    public boolean isCgpaSatisfied() {
        return cgpaSatisfied;
    }

    public void setCgpaSatisfied(boolean cgpaSatisfied) {
        this.cgpaSatisfied = cgpaSatisfied;
    }

    public String getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(String studentDepartment) {
        this.studentDepartment = studentDepartment;
    }

    public String getEligibleDepartments() {
        return eligibleDepartments;
    }

    public void setEligibleDepartments(String eligibleDepartments) {
        this.eligibleDepartments = eligibleDepartments;
    }

    public boolean isDepartmentSatisfied() {
        return departmentSatisfied;
    }

    public void setDepartmentSatisfied(boolean departmentSatisfied) {
        this.departmentSatisfied = departmentSatisfied;
    }

    public Integer getStudentGraduationYear() {
        return studentGraduationYear;
    }

    public void setStudentGraduationYear(Integer studentGraduationYear) {
        this.studentGraduationYear = studentGraduationYear;
    }

    public String getEligibleGraduationYears() {
        return eligibleGraduationYears;
    }

    public void setEligibleGraduationYears(String eligibleGraduationYears) {
        this.eligibleGraduationYears = eligibleGraduationYears;
    }

    public boolean isGraduationYearSatisfied() {
        return graduationYearSatisfied;
    }

    public void setGraduationYearSatisfied(boolean graduationYearSatisfied) {
        this.graduationYearSatisfied = graduationYearSatisfied;
    }

    public boolean isDeadlinePassed() {
        return deadlinePassed;
    }

    public void setDeadlinePassed(boolean deadlinePassed) {
        this.deadlinePassed = deadlinePassed;
    }

    public boolean isAlreadyApplied() {
        return alreadyApplied;
    }

    public void setAlreadyApplied(boolean alreadyApplied) {
        this.alreadyApplied = alreadyApplied;
    }

    public boolean isProfileIncomplete() {
        return profileIncomplete;
    }

    public void setProfileIncomplete(boolean profileIncomplete) {
        this.profileIncomplete = profileIncomplete;
    }
}
