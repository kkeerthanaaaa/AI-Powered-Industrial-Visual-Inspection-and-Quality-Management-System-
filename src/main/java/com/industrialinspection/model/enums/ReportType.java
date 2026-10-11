package com.industrialinspection.model.enums;

public enum ReportType {
    DAILY_INSPECTION("Daily Inspection Report"),
    BATCH_QUALITY("Batch Quality Report"),
    PRODUCT_DEFECT("Product Defect Analysis"),
    OPERATOR_PERFORMANCE("Operator Performance Report"),
    MACHINE_DEFECT("Machine-wise Defect Report"),
    MONTHLY_SUMMARY("Monthly Quality Summary"),
    PRODUCTION_TREND("Production Quality Trend");

    private final String title;

    ReportType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static ReportType fromString(String val) {
        if (val == null) return DAILY_INSPECTION;
        try {
            return ReportType.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return DAILY_INSPECTION;
        }
    }
}
