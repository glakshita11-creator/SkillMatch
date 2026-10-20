package com.skillmatch.dto;

import com.skillmatch.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ApplicationStatusUpdateRequest {

    @NotNull(message = "New application status is required")
    private ApplicationStatus status;

    private String recruiterNotes;
    private LocalDateTime interviewDate;

    public ApplicationStatusUpdateRequest() {
    }

    public ApplicationStatusUpdateRequest(ApplicationStatus status, String recruiterNotes) {
        this.status = status;
        this.recruiterNotes = recruiterNotes;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
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
}
