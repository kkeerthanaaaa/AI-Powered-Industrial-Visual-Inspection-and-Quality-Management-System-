package com.industrialinspection.model.enums;

public enum OperatorStatus {
    ACTIVE,
    INACTIVE,
    ON_LEAVE;

    public static OperatorStatus fromString(String val) {
        if (val == null) return ACTIVE;
        try {
            return OperatorStatus.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE;
        }
    }
}
