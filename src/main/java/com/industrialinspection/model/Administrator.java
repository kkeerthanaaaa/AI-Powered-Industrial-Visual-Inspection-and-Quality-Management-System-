package com.industrialinspection.model;

import com.industrialinspection.model.enums.UserRole;

/**
 * Administrator role with full privileges.
 */
public class Administrator extends User {
    private String adminDepartment = "System Administration";

    public Administrator() {
        super();
        setRole(UserRole.ADMINISTRATOR);
    }

    public Administrator(int userId, String username, String fullName, String email) {
        super(userId, username, fullName, email, UserRole.ADMINISTRATOR);
    }

    public String getAdminDepartment() {
        return adminDepartment;
    }

    public void setAdminDepartment(String adminDepartment) {
        this.adminDepartment = adminDepartment;
    }

    @Override
    public boolean canManageUsers() {
        return true;
    }

    @Override
    public boolean canBackupDatabase() {
        return true;
    }
}
