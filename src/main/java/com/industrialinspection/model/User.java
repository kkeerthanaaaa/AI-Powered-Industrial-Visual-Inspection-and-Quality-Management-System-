package com.industrialinspection.model;

import com.industrialinspection.model.enums.UserRole;
import java.sql.Timestamp;

/**
 * Base User model demonstrating Encapsulation and Polymorphism.
 */
public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String email;
    private UserRole role;
    private String status;
    private int failedAttempts;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {
        this.status = "ACTIVE";
        this.failedAttempts = 0;
    }

    public User(int userId, String username, String fullName, String email, UserRole role) {
        this();
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    // Role-based privilege checks (Polymorphic defaults)
    public boolean canManageUsers() {
        return this.role == UserRole.ADMINISTRATOR;
    }

    public boolean canManageMachines() {
        return this.role == UserRole.ADMINISTRATOR;
    }

    public boolean canManageOperators() {
        return this.role == UserRole.ADMINISTRATOR;
    }

    public boolean canManageProducts() {
        return this.role == UserRole.ADMINISTRATOR || this.role == UserRole.QUALITY_ENGINEER;
    }

    public boolean canManageBatches() {
        return this.role == UserRole.ADMINISTRATOR || this.role == UserRole.QUALITY_ENGINEER;
    }

    public boolean canPerformInspection() {
        return this.role == UserRole.ADMINISTRATOR || this.role == UserRole.QUALITY_ENGINEER || this.role == UserRole.INSPECTION_OPERATOR;
    }

    public boolean canApproveInspection() {
        return this.role == UserRole.ADMINISTRATOR || this.role == UserRole.QUALITY_ENGINEER;
    }

    public boolean canCreateCorrectiveAction() {
        return this.role == UserRole.ADMINISTRATOR || this.role == UserRole.QUALITY_ENGINEER;
    }

    public boolean canGenerateReports() {
        return this.role == UserRole.ADMINISTRATOR || this.role == UserRole.QUALITY_ENGINEER || this.role == UserRole.PRODUCTION_SUPERVISOR;
    }

    public boolean canBackupDatabase() {
        return this.role == UserRole.ADMINISTRATOR;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return fullName + " (" + (role != null ? role.getDisplayName() : "Unknown") + ")";
    }
}
