package com.industrialinspection.model.enums;

public enum ActionStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    VERIFIED;

    public static ActionStatus fromString(String val) {
        if (val == null) return OPEN;
        try {
            return ActionStatus.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OPEN;
        }
    }
}
