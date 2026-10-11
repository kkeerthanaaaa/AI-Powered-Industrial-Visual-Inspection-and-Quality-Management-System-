package com.industrialinspection.model;

import com.industrialinspection.model.enums.ReportType;
import java.sql.Timestamp;

public class QualityReport {
    private int reportId;
    private ReportType reportType;
    private String title;
    private int generatedBy;
    private String parametersJson;
    private String filePath;
    private String fileFormat;
    private Timestamp generatedAt;

    // Helper
    private String generatedByName;

    public QualityReport() {
        this.fileFormat = "CSV";
    }

    public QualityReport(int reportId, ReportType reportType, String title, int generatedBy, 
                         String filePath, String fileFormat) {
        this();
        this.reportId = reportId;
        this.reportType = reportType;
        this.title = title;
        this.generatedBy = generatedBy;
        this.filePath = filePath;
        this.fileFormat = fileFormat;
    }

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(int generatedBy) { this.generatedBy = generatedBy; }

    public String getParametersJson() { return parametersJson; }
    public void setParametersJson(String parametersJson) { this.parametersJson = parametersJson; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }

    public Timestamp getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Timestamp generatedAt) { this.generatedAt = generatedAt; }

    public String getGeneratedByName() { return generatedByName; }
    public void setGeneratedByName(String generatedByName) { this.generatedByName = generatedByName; }

    @Override
    public String toString() {
        return title + " (" + fileFormat + ") - " + generatedAt;
    }
}
