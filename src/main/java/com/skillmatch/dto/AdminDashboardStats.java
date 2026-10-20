package com.skillmatch.dto;

import java.util.Map;

public class AdminDashboardStats {
    private long totalStudents;
    private long totalCompanies;
    private long totalOpportunities;
    private long activeOpportunities;
    private long totalApplications;
    private long selectedCount;
    private long shortlistedCount;
    private long activeUsersCount;
    private double placementRate;
    private Map<String, Long> applicationsByStatus;

    public AdminDashboardStats() {
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalCompanies() {
        return totalCompanies;
    }

    public void setTotalCompanies(long totalCompanies) {
        this.totalCompanies = totalCompanies;
    }

    public long getTotalOpportunities() {
        return totalOpportunities;
    }

    public void setTotalOpportunities(long totalOpportunities) {
        this.totalOpportunities = totalOpportunities;
    }

    public long getActiveOpportunities() {
        return activeOpportunities;
    }

    public void setActiveOpportunities(long activeOpportunities) {
        this.activeOpportunities = activeOpportunities;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(long selectedCount) {
        this.selectedCount = selectedCount;
    }

    public long getShortlistedCount() {
        return shortlistedCount;
    }

    public void setShortlistedCount(long shortlistedCount) {
        this.shortlistedCount = shortlistedCount;
    }

    public long getActiveUsersCount() {
        return activeUsersCount;
    }

    public void setActiveUsersCount(long activeUsersCount) {
        this.activeUsersCount = activeUsersCount;
    }

    public double getPlacementRate() {
        return placementRate;
    }

    public void setPlacementRate(double placementRate) {
        this.placementRate = placementRate;
    }

    public Map<String, Long> getApplicationsByStatus() {
        return applicationsByStatus;
    }

    public void setApplicationsByStatus(Map<String, Long> applicationsByStatus) {
        this.applicationsByStatus = applicationsByStatus;
    }
}
