package com.industrialinspection.model.enums;

/**
 * Roles for role-based authorization in ABC Precision Manufacturing.
 */
public enum UserRole {
    ADMINISTRATOR("Administrator", "Full system administration, user management, and configuration"),
    QUALITY_ENGINEER("Quality Engineer", "Defect classification, inspection review, CAPA, and quality reports"),
    PRODUCTION_SUPERVISOR("Production Supervisor", "Batch monitoring, machine/operator tracking, and quality metrics"),
    INSPECTION_OPERATOR("Inspection Operator", "Visual inspection execution, image capture, and logging");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromString(String roleStr) {
        if (roleStr == null) return INSPECTION_OPERATOR;
        for (UserRole r : values()) {
            if (r.name().equalsIgnoreCase(roleStr.trim()) || r.displayName.equalsIgnoreCase(roleStr.trim())) {
                return r;
            }
        }
        return INSPECTION_OPERATOR;
    }
}
