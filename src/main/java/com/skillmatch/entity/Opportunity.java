package com.skillmatch.entity;

import com.skillmatch.enums.OpportunityStatus;
import com.skillmatch.enums.OpportunityType;
import com.skillmatch.enums.WorkplaceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunities", indexes = {
    @Index(name = "idx_opp_status", columnList = "status"),
    @Index(name = "idx_opp_deadline", columnList = "deadline"),
    @Index(name = "idx_opp_type", columnList = "opportunity_type")
})
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotBlank(message = "Job title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Job description is required")
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String description;

    @NotNull(message = "Opportunity type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "opportunity_type", nullable = false, length = 30)
    private OpportunityType opportunityType;

    @NotNull(message = "Workplace type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "workplace_type", nullable = false, length = 30)
    private WorkplaceType workplaceType = WorkplaceType.ON_SITE;

    @NotBlank(message = "Location is required")
    @Column(nullable = false, length = 150)
    private String location;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @DecimalMin(value = "0.00", message = "Minimum CGPA cannot be negative")
    @Column(name = "min_cgpa", precision = 4, scale = 2, nullable = false)
    private BigDecimal minCgpa = BigDecimal.ZERO;

    @Column(name = "eligible_departments", columnDefinition = "TEXT")
    private String eligibleDepartments; // Comma-separated (e.g. "Computer Science, Information Technology")

    @Column(name = "eligible_graduation_years", length = 255)
    private String eligibleGraduationYears; // Comma-separated (e.g. "2024, 2025, 2026")

    @Column(name = "salary_or_stipend", length = 100)
    private String salaryOrStipend;

    @Min(value = 1, message = "Openings must be at least 1")
    @Column(nullable = false)
    private Integer openings = 1;

    @NotNull(message = "Application deadline is required")
    @Column(nullable = false)
    private LocalDate deadline;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OpportunityStatus status = OpportunityStatus.PUBLISHED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Opportunity() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OpportunityStatus.PUBLISHED;
        }
        if (this.workplaceType == null) {
            this.workplaceType = WorkplaceType.ON_SITE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OpportunityType getOpportunityType() {
        return opportunityType;
    }

    public void setOpportunityType(OpportunityType opportunityType) {
        this.opportunityType = opportunityType;
    }

    public WorkplaceType getWorkplaceType() {
        return workplaceType;
    }

    public void setWorkplaceType(WorkplaceType workplaceType) {
        this.workplaceType = workplaceType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public BigDecimal getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(BigDecimal minCgpa) {
        this.minCgpa = minCgpa;
    }

    public String getEligibleDepartments() {
        return eligibleDepartments;
    }

    public void setEligibleDepartments(String eligibleDepartments) {
        this.eligibleDepartments = eligibleDepartments;
    }

    public String getEligibleGraduationYears() {
        return eligibleGraduationYears;
    }

    public void setEligibleGraduationYears(String eligibleGraduationYears) {
        this.eligibleGraduationYears = eligibleGraduationYears;
    }

    public String getSalaryOrStipend() {
        return salaryOrStipend;
    }

    public void setSalaryOrStipend(String salaryOrStipend) {
        this.salaryOrStipend = salaryOrStipend;
    }

    public Integer getOpenings() {
        return openings;
    }

    public void setOpenings(Integer openings) {
        this.openings = openings;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public OpportunityStatus getStatus() {
        return status;
    }

    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
