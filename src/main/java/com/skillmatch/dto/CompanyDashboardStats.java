package com.skillmatch.dto;

public class CompanyDashboardStats {
    private long totalOpportunities;
    private long activeOpportunities;
    private long totalApplicants;
    private long underReviewCount;
    private long shortlistedCount;
    private long interviewScheduledCount;
    private long selectedCount;
    private long rejectedCount;

    public CompanyDashboardStats() {
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

    public long getTotalApplicants() {
        return totalApplicants;
    }

    public void setTotalApplicants(long totalApplicants) {
        this.totalApplicants = totalApplicants;
    }

    public long getUnderReviewCount() {
        return underReviewCount;
    }

    public void setUnderReviewCount(long underReviewCount) {
        this.underReviewCount = underReviewCount;
    }

    public long getShortlistedCount() {
        return shortlistedCount;
    }

    public void setShortlistedCount(long shortlistedCount) {
        this.shortlistedCount = shortlistedCount;
    }

    public long getInterviewScheduledCount() {
        return interviewScheduledCount;
    }

    public void setInterviewScheduledCount(long interviewScheduledCount) {
        this.interviewScheduledCount = interviewScheduledCount;
    }

    public long getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(long selectedCount) {
        this.selectedCount = selectedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }
}
