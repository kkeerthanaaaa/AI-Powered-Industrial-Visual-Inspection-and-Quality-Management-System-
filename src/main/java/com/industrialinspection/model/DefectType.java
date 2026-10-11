package com.industrialinspection.model;

import com.industrialinspection.model.enums.SeverityLevel;
import java.sql.Timestamp;

public class DefectType {
    private int defectTypeId;
    private String defectCode;
    private String defectName;
    private String category;
    private SeverityLevel defaultSeverity;
    private String description;
    private String standardRemedy;
    private Timestamp createdAt;

    public DefectType() {
        this.defaultSeverity = SeverityLevel.MEDIUM;
    }

    public DefectType(int defectTypeId, String defectCode, String defectName, String category, 
                      SeverityLevel defaultSeverity, String description, String standardRemedy) {
        this.defectTypeId = defectTypeId;
        this.defectCode = defectCode;
        this.defectName = defectName;
        this.category = category;
        this.defaultSeverity = defaultSeverity;
        this.description = description;
        this.standardRemedy = standardRemedy;
    }

    public int getDefectTypeId() { return defectTypeId; }
    public void setDefectTypeId(int defectTypeId) { this.defectTypeId = defectTypeId; }

    public String getDefectCode() { return defectCode; }
    public void setDefectCode(String defectCode) { this.defectCode = defectCode; }

    public String getDefectName() { return defectName; }
    public void setDefectName(String defectName) { this.defectName = defectName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public SeverityLevel getDefaultSeverity() { return defaultSeverity; }
    public void setDefaultSeverity(SeverityLevel defaultSeverity) { this.defaultSeverity = defaultSeverity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStandardRemedy() { return standardRemedy; }
    public void setStandardRemedy(String standardRemedy) { this.standardRemedy = standardRemedy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return defectName + " (" + defectCode + ")";
    }
}
