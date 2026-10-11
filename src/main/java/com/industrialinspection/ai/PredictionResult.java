package com.industrialinspection.ai;

import com.industrialinspection.model.enums.DefectLocation;
import com.industrialinspection.model.enums.SeverityLevel;

/**
 * Encapsulates the output of a visual inspection analysis run.
 */
public class PredictionResult {

    private final String prediction;
    private final double confidenceScore;
    private final String defectType;
    private final SeverityLevel severity;
    private final DefectLocation defectLocation;
    private final int boundingBoxX;
    private final int boundingBoxY;
    private final int boundingBoxW;
    private final int boundingBoxH;
    private final int processingTimeMs;
    private final String rawResponseJson;

    public PredictionResult(String prediction, double confidenceScore, String defectType, 
                            SeverityLevel severity, DefectLocation defectLocation, 
                            int boundingBoxX, int boundingBoxY, int boundingBoxW, int boundingBoxH, 
                            int processingTimeMs, String rawResponseJson) {
        this.prediction = prediction;
        this.confidenceScore = confidenceScore;
        this.defectType = defectType;
        this.severity = severity;
        this.defectLocation = defectLocation;
        this.boundingBoxX = boundingBoxX;
        this.boundingBoxY = boundingBoxY;
        this.boundingBoxW = boundingBoxW;
        this.boundingBoxH = boundingBoxH;
        this.processingTimeMs = processingTimeMs;
        this.rawResponseJson = rawResponseJson;
    }

    public boolean isDefectFree() {
        return "DEFECT_FREE".equalsIgnoreCase(prediction) || "DEFECT_FREE".equalsIgnoreCase(defectType);
    }

    public String getPrediction() { return prediction; }
    public double getConfidenceScore() { return confidenceScore; }
    public String getDefectType() { return defectType; }
    public SeverityLevel getSeverity() { return severity; }
    public DefectLocation getDefectLocation() { return defectLocation; }
    public int getBoundingBoxX() { return boundingBoxX; }
    public int getBoundingBoxY() { return boundingBoxY; }
    public int getBoundingBoxW() { return boundingBoxW; }
    public int getBoundingBoxH() { return boundingBoxH; }
    public int getProcessingTimeMs() { return processingTimeMs; }
    public String getRawResponseJson() { return rawResponseJson; }

    @Override
    public String toString() {
        return String.format("%s (%.2f%%) - %s at %s [%d ms]", 
                prediction, confidenceScore, severity, defectLocation, processingTimeMs);
    }
}
