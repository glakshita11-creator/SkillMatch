package com.skillmatch.enums;

public enum Role {
    ROLE_STUDENT,
    ROLE_COMPANY,
    ROLE_ADMIN;

    public String getCleanName() {
        return name().replace("ROLE_", "");
    }
}
