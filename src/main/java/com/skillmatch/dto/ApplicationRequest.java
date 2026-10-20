package com.skillmatch.dto;

public class ApplicationRequest {
    private String coverNote;

    public ApplicationRequest() {
    }

    public ApplicationRequest(String coverNote) {
        this.coverNote = coverNote;
    }

    public String getCoverNote() {
        return coverNote;
    }

    public void setCoverNote(String coverNote) {
        this.coverNote = coverNote;
    }
}
