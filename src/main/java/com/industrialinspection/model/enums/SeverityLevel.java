package com.industrialinspection.model.enums;

public enum SeverityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static SeverityLevel fromString(String val) {
        if (val == null) return MEDIUM;
        try {
            return SeverityLevel.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEDIUM;
        }
    }
}
