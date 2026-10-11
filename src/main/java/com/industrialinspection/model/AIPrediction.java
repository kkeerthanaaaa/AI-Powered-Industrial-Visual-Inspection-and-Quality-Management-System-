package com.industrialinspection.model;

import com.industrialinspection.model.enums.DefectLocation;
import com.industrialinspection.model.enums.SeverityLevel;
import java.sql.Timestamp;

public class AIPrediction {
    private int predictionId;
    private int imageId;
    private String predictedLabel;
    private double confidenceScore;
    private String defectType;
    private SeverityLevel severity;
    private DefectLocation defectLocation;
    private int boundingBoxX;
    private int boundingBoxY;
    private int boundingBoxW;
    private int boundingBoxH;
    private int processingTimeMs;
    private String rawResponseJson;
    private Timestamp createdAt;

    public AIPrediction() {
        this.severity = SeverityLevel.LOW;
        this.defectLocation = DefectLocation.CENTER;
        this.processingTimeMs = 45;
    }

    public AIPrediction(int predictionId, int imageId, String predictedLabel, double confidenceScore,
                        String defectType, SeverityLevel severity, DefectLocation defectLocation,
                        int boundingBoxX, int boundingBoxY, int boundingBoxW, int boundingBoxH,
                        int processingTimeMs) {
        this.predictionId = predictionId;
        this.imageId = imageId;
        this.predictedLabel = predictedLabel;
        this.confidenceScore = confidenceScore;
        this.defectType = defectType;
        this.severity = severity;
        this.defectLocation = defectLocation;
        this.boundingBoxX = boundingBoxX;
        this.boundingBoxY = boundingBoxY;
        this.boundingBoxW = boundingBoxW;
        this.boundingBoxH = boundingBoxH;
        this.processingTimeMs = processingTimeMs;
    }

    public boolean isDefectFree() {
        return "DEFECT_FREE".equalsIgnoreCase(predictedLabel) || "DEFECT_FREE".equalsIgnoreCase(defectType);
    }

    public int getPredictionId() { return predictionId; }
    public void setPredictionId(int predictionId) { this.predictionId = predictionId; }

    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }

    public String getPredictedLabel() { return predictedLabel; }
    public void setPredictedLabel(String predictedLabel) { this.predictedLabel = predictedLabel; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getDefectType() { return defectType; }
    public void setDefectType(String defectType) { this.defectType = defectType; }

    public SeverityLevel getSeverity() { return severity; }
    public void setSeverity(SeverityLevel severity) { this.severity = severity; }

    public DefectLocation getDefectLocation() { return defectLocation; }
    public void setDefectLocation(DefectLocation defectLocation) { this.defectLocation = defectLocation; }

    public int getBoundingBoxX() { return boundingBoxX; }
    public void setBoundingBoxX(int boundingBoxX) { this.boundingBoxX = boundingBoxX; }

    public int getBoundingBoxY() { return boundingBoxY; }
    public void setBoundingBoxY(int boundingBoxY) { this.boundingBoxY = boundingBoxY; }

    public int getBoundingBoxW() { return boundingBoxW; }
    public void setBoundingBoxW(int boundingBoxW) { this.boundingBoxW = boundingBoxW; }

    public int getBoundingBoxH() { return boundingBoxH; }
    public void setBoundingBoxH(int boundingBoxH) { this.boundingBoxH = boundingBoxH; }

    public int getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(int processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public String getRawResponseJson() { return rawResponseJson; }
    public void setRawResponseJson(String rawResponseJson) { this.rawResponseJson = rawResponseJson; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return predictedLabel + " (" + String.format("%.1f%%", confidenceScore) + ")";
    }
}
