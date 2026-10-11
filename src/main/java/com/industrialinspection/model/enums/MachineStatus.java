package com.industrialinspection.model.enums;

public enum MachineStatus {
    ACTIVE,
    INACTIVE,
    MAINTENANCE;

    public static MachineStatus fromString(String val) {
        if (val == null) return ACTIVE;
        try {
            return MachineStatus.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE;
        }
    }
}
