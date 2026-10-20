package com.skillmatch.exception;

import java.util.ArrayList;
import java.util.List;

public class IneligibleApplicationException extends RuntimeException {
    private final List<String> reasons;

    public IneligibleApplicationException(String message, List<String> reasons) {
        super(message);
        this.reasons = reasons != null ? reasons : new ArrayList<>();
    }

    public List<String> getReasons() {
        return reasons;
    }
}
