package com.industrialinspection.model;

import com.industrialinspection.model.enums.UserRole;

/**
 * Inspection Operator executing visual inspection and optical image captures on the floor.
 */
public class InspectionOperator extends User {
    private String stationId = "STATION-OPTICAL-01";

    public InspectionOperator() {
        super();
        setRole(UserRole.INSPECTION_OPERATOR);
    }

    public InspectionOperator(int userId, String username, String fullName, String email) {
        super(userId, username, fullName, email, UserRole.INSPECTION_OPERATOR);
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }
}
