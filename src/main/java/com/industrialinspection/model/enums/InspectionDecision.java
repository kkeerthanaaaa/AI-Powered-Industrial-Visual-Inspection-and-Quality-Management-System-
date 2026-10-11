package com.industrialinspection.model.enums;

public enum InspectionDecision {
    PASS("Pass - Conforms to Quality Standards"),
    FAIL("Fail - Reject / Non-conformance Detected"),
    REQUIRES_REVIEW("Requires Review - Secondary Verification Needed");

    private final String description;

    InspectionDecision(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static InspectionDecision fromString(String val) {
        if (val == null) return REQUIRES_REVIEW;
        try {
            return InspectionDecision.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return REQUIRES_REVIEW;
        }
    }
}
