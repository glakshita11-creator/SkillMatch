package com.skillmatch.dto;

public class StudentDashboardStats {
    private long totalApplications;
    private long underReviewCount;
    private long shortlistedCount;
    private long interviewScheduledCount;
    private long selectedCount;
    private long rejectedCount;
    private long activeOpportunitiesCount;
    private boolean profileComplete;

    public StudentDashboardStats() {
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
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

    public long getActiveOpportunitiesCount() {
        return activeOpportunitiesCount;
    }

    public void setActiveOpportunitiesCount(long activeOpportunitiesCount) {
        this.activeOpportunitiesCount = activeOpportunitiesCount;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.profileComplete = profileComplete;
    }
}
