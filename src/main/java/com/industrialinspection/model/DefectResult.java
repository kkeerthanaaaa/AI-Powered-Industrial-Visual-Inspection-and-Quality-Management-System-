package com.industrialinspection.model;

import com.industrialinspection.model.enums.DefectLocation;
import com.industrialinspection.model.enums.SeverityLevel;
import java.sql.Timestamp;

public class DefectResult {
    private int defectResultId;
    private int inspectionId;
    private int defectTypeId;
    private SeverityLevel severity;
    private DefectLocation location;
    private int defectX;
    private int defectY;
    private int defectW;
    private int defectH;
    private String comments;
    private Timestamp createdAt;

    // Helpers
    private String defectCode;
    private String defectName;

    public DefectResult() {
        this.severity = SeverityLevel.MEDIUM;
        this.location = DefectLocation.CENTER;
    }

    public DefectResult(int defectResultId, int inspectionId, int defectTypeId, 
                        SeverityLevel severity, DefectLocation location, String comments) {
        this();
        this.defectResultId = defectResultId;
        this.inspectionId = inspectionId;
        this.defectTypeId = defectTypeId;
        this.severity = severity;
        this.location = location;
        this.comments = comments;
    }

    public int getDefectResultId() { return defectResultId; }
    public void setDefectResultId(int defectResultId) { this.defectResultId = defectResultId; }

    public int getInspectionId() { return inspectionId; }
    public void setInspectionId(int inspectionId) { this.inspectionId = inspectionId; }

    public int getDefectTypeId() { return defectTypeId; }
    public void setDefectTypeId(int defectTypeId) { this.defectTypeId = defectTypeId; }

    public SeverityLevel getSeverity() { return severity; }
    public void setSeverity(SeverityLevel severity) { this.severity = severity; }

    public DefectLocation getLocation() { return location; }
    public void setLocation(DefectLocation location) { this.location = location; }

    public int getDefectX() { return defectX; }
    public void setDefectX(int defectX) { this.defectX = defectX; }

    public int getDefectY() { return defectY; }
    public void setDefectY(int defectY) { this.defectY = defectY; }

    public int getDefectW() { return defectW; }
    public void setDefectW(int defectW) { this.defectW = defectW; }

    public int getDefectH() { return defectH; }
    public void setDefectH(int defectH) { this.defectH = defectH; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getDefectCode() { return defectCode; }
    public void setDefectCode(String defectCode) { this.defectCode = defectCode; }

    public String getDefectName() { return defectName; }
    public void setDefectName(String defectName) { this.defectName = defectName; }

    @Override
    public String toString() {
        return (defectName != null ? defectName : "Defect #" + defectTypeId) + " [" + severity + "] at " + location;
    }
}
