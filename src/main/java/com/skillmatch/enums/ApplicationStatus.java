package com.skillmatch.enums;

import java.util.List;

public enum ApplicationStatus {
    APPLIED("Applied", "Application submitted and awaiting recruiter review"),
    UNDER_REVIEW("Under Review", "Recruiter is currently reviewing candidate credentials"),
    SHORTLISTED("Shortlisted", "Candidate shortlisted for technical assessments or interviews"),
    INTERVIEW_SCHEDULED("Interview Scheduled", "Interview rounds have been coordinated"),
    SELECTED("Selected", "Candidate has received a placement or internship offer"),
    REJECTED("Rejected", "Application was not progressed further");

    private final String displayName;
    private final String description;

    ApplicationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == SELECTED || this == REJECTED;
    }

    public boolean canWithdraw() {
        return !isTerminal();
    }

    public static List<ApplicationStatus> getAllowedNextStatuses(ApplicationStatus current) {
        if (current == null) {
            return List.of(APPLIED);
        }
        return switch (current) {
            case APPLIED -> List.of(UNDER_REVIEW, SHORTLISTED, INTERVIEW_SCHEDULED, REJECTED);
            case UNDER_REVIEW -> List.of(SHORTLISTED, INTERVIEW_SCHEDULED, REJECTED);
            case SHORTLISTED -> List.of(INTERVIEW_SCHEDULED, SELECTED, REJECTED);
            case INTERVIEW_SCHEDULED -> List.of(SELECTED, REJECTED);
            case SELECTED, REJECTED -> List.of();
        };
    }

}
