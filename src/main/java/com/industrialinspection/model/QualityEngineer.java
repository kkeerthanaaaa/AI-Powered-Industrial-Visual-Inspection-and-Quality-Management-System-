package com.industrialinspection.model;

import com.industrialinspection.model.enums.UserRole;

/**
 * Quality Engineer role specializing in defect classification, inspection approval, and CAPA.
 */
public class QualityEngineer extends User {
    private String certificationLevel = "Six Sigma Black Belt";

    public QualityEngineer() {
        super();
        setRole(UserRole.QUALITY_ENGINEER);
    }

    public QualityEngineer(int userId, String username, String fullName, String email) {
        super(userId, username, fullName, email, UserRole.QUALITY_ENGINEER);
    }

    public String getCertificationLevel() {
        return certificationLevel;
    }

    public void setCertificationLevel(String certificationLevel) {
        this.certificationLevel = certificationLevel;
    }

    @Override
    public boolean canApproveInspection() {
        return true;
    }

    @Override
    public boolean canCreateCorrectiveAction() {
        return true;
    }
}
