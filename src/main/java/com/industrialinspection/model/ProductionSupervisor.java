package com.industrialinspection.model;

import com.industrialinspection.model.enums.UserRole;

/**
 * Production Supervisor monitoring floor line throughput and batch metrics.
 */
public class ProductionSupervisor extends User {
    private String assignedPlantArea = "Line 1 & 2 Manufacturing Cell";

    public ProductionSupervisor() {
        super();
        setRole(UserRole.PRODUCTION_SUPERVISOR);
    }

    public ProductionSupervisor(int userId, String username, String fullName, String email) {
        super(userId, username, fullName, email, UserRole.PRODUCTION_SUPERVISOR);
    }

    public String getAssignedPlantArea() {
        return assignedPlantArea;
    }

    public void setAssignedPlantArea(String assignedPlantArea) {
        this.assignedPlantArea = assignedPlantArea;
    }
}
