package com.industrialinspection.model.enums;

public enum BatchStatus {
    PENDING,
    IN_INSPECTION,
    APPROVED,
    REJECTED,
    QUARANTINED;

    public static BatchStatus fromString(String val) {
        if (val == null) return PENDING;
        try {
            return BatchStatus.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
