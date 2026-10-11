package com.industrialinspection.model.enums;

public enum Shift {
    MORNING,
    EVENING,
    NIGHT;

    public static Shift fromString(String val) {
        if (val == null) return MORNING;
        try {
            return Shift.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MORNING;
        }
    }
}
