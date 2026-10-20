package com.skillmatch.dto;

import com.skillmatch.enums.ApplicationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ApplicationResponse {
    private Long id;
    private Long opportunityId;
    private String opportunityTitle;
    private String opportunityType;
    private String location;
    private String salaryOrStipend;
    private Long companyId;
    private String companyName;

    // Student fields
    private Long studentProfileId;
    private Long studentUserId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String studentDepartment;
    private String studentDegree;
    private Integer studentGraduationYear;
    private BigDecimal studentCgpa;
    private String studentSkills;
    private boolean hasResume;
    private String resumeOriginalName;
    private String resumeUrl;

    // Application state
    private ApplicationStatus status;
    private String statusDisplayName;
    private String coverNote;
    private String recruiterNotes;
    private LocalDateTime interviewDate;
    private boolean withdrawn;
    private boolean canWithdraw;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    public ApplicationResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(Long opportunityId) {
        this.opportunityId = opportunityId;
    }

    public String getOpportunityTitle() {
        return opportunityTitle;
    }

    public void setOpportunityTitle(String opportunityTitle) {
        this.opportunityTitle = opportunityTitle;
    }

    public String getOpportunityType() {
        return opportunityType;
    }

    public void setOpportunityType(String opportunityType) {
        this.opportunityType = opportunityType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSalaryOrStipend() {
        return salaryOrStipend;
    }

    public void setSalaryOrStipend(String salaryOrStipend) {
        this.salaryOrStipend = salaryOrStipend;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getStudentProfileId() {
        return studentProfileId;
    }

    public void setStudentProfileId(Long studentProfileId) {
        this.studentProfileId = studentProfileId;
    }

    public Long getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(Long studentUserId) {
        this.studentUserId = studentUserId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentPhone() {
        return studentPhone;
    }

    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
    }

    public String getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(String studentDepartment) {
        this.studentDepartment = studentDepartment;
    }

    public String getStudentDegree() {
        return studentDegree;
    }

    public void setStudentDegree(String studentDegree) {
        this.studentDegree = studentDegree;
    }

    public Integer getStudentGraduationYear() {
        return studentGraduationYear;
    }

    public void setStudentGraduationYear(Integer studentGraduationYear) {
        this.studentGraduationYear = studentGraduationYear;
    }

    public BigDecimal getStudentCgpa() {
        return studentCgpa;
    }

    public void setStudentCgpa(BigDecimal studentCgpa) {
        this.studentCgpa = studentCgpa;
    }

    public String getStudentSkills() {
        return studentSkills;
    }

    public void setStudentSkills(String studentSkills) {
        this.studentSkills = studentSkills;
    }

    public boolean isHasResume() {
        return hasResume;
    }

    public void setHasResume(boolean hasResume) {
        this.hasResume = hasResume;
    }

    public String getResumeOriginalName() {
        return resumeOriginalName;
    }

    public void setResumeOriginalName(String resumeOriginalName) {
        this.resumeOriginalName = resumeOriginalName;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
        if (status != null) {
            this.statusDisplayName = status.getDisplayName();
            this.canWithdraw = status.canWithdraw();
        }
    }

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }

    public String getCoverNote() {
        return coverNote;
    }

    public void setCoverNote(String coverNote) {
        this.coverNote = coverNote;
    }

    public String getRecruiterNotes() {
        return recruiterNotes;
    }

    public void setRecruiterNotes(String recruiterNotes) {
        this.recruiterNotes = recruiterNotes;
    }

    public LocalDateTime getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDateTime interviewDate) {
        this.interviewDate = interviewDate;
    }

    public boolean isWithdrawn() {
        return withdrawn;
    }

    public void setWithdrawn(boolean withdrawn) {
        this.withdrawn = withdrawn;
    }

    public boolean isCanWithdraw() {
        return canWithdraw;
    }

    public void setCanWithdraw(boolean canWithdraw) {
        this.canWithdraw = canWithdraw;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
