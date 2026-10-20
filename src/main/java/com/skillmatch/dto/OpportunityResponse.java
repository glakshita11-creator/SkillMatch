package com.skillmatch.dto;

import com.skillmatch.enums.OpportunityStatus;
import com.skillmatch.enums.OpportunityType;
import com.skillmatch.enums.WorkplaceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OpportunityResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private String companyLocation;
    private String companyWebsite;
    private String title;
    private String description;
    private OpportunityType opportunityType;
    private WorkplaceType workplaceType;
    private String location;
    private String requiredSkills;
    private BigDecimal minCgpa;
    private String eligibleDepartments;
    private String eligibleGraduationYears;
    private String salaryOrStipend;
    private Integer openings;
    private LocalDate deadline;
    private OpportunityStatus status;
    private LocalDateTime createdAt;
    private long applicationCount;
    private boolean eligibleForCurrentUser = true;
    private List<String> eligibilityReasons = new ArrayList<>();
    private boolean alreadyApplied = false;
    private Long userApplicationId;
    private boolean expired = false;

    public OpportunityResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCompanyLocation() {
        return companyLocation;
    }

    public void setCompanyLocation(String companyLocation) {
        this.companyLocation = companyLocation;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
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
        if (deadline != null) {
            this.expired = deadline.isBefore(LocalDate.now());
        }
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

    public long getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(long applicationCount) {
        this.applicationCount = applicationCount;
    }

    public boolean isEligibleForCurrentUser() {
        return eligibleForCurrentUser;
    }

    public void setEligibleForCurrentUser(boolean eligibleForCurrentUser) {
        this.eligibleForCurrentUser = eligibleForCurrentUser;
    }

    public List<String> getEligibilityReasons() {
        return eligibilityReasons;
    }

    public void setEligibilityReasons(List<String> eligibilityReasons) {
        this.eligibilityReasons = eligibilityReasons;
    }

    public boolean isAlreadyApplied() {
        return alreadyApplied;
    }

    public void setAlreadyApplied(boolean alreadyApplied) {
        this.alreadyApplied = alreadyApplied;
    }

    public Long getUserApplicationId() {
        return userApplicationId;
    }

    public void setUserApplicationId(Long userApplicationId) {
        this.userApplicationId = userApplicationId;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}
