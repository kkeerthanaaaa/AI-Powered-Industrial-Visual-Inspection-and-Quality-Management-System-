package com.industrialinspection.model;

import com.industrialinspection.model.enums.InspectionDecision;
import java.sql.Timestamp;

public class InspectionRecord {
    private int inspectionId;
    private int productId;
    private int batchId;
    private int imageId;
    private int inspectorId;
    private Integer predictionId;
    private Timestamp inspectionDate;
    private InspectionDecision finalDecision;
    private String remarks;
    private String status;
    private Timestamp createdAt;

    // Associated UI helper fields
    private String productName;
    private String batchNumber;
    private String inspectorName;
    private String predictedLabel;
    private double confidenceScore;
    private String imagePath;

    public InspectionRecord() {
        this.finalDecision = InspectionDecision.REQUIRES_REVIEW;
        this.status = "SUBMITTED";
    }

    public InspectionRecord(int inspectionId, int productId, int batchId, int imageId, 
                            int inspectorId, Integer predictionId, InspectionDecision finalDecision, String remarks) {
        this();
        this.inspectionId = inspectionId;
        this.productId = productId;
        this.batchId = batchId;
        this.imageId = imageId;
        this.inspectorId = inspectorId;
        this.predictionId = predictionId;
        this.finalDecision = finalDecision;
        this.remarks = remarks;
    }

    public int getInspectionId() { return inspectionId; }
    public void setInspectionId(int inspectionId) { this.inspectionId = inspectionId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }

    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }

    public int getInspectorId() { return inspectorId; }
    public void setInspectorId(int inspectorId) { this.inspectorId = inspectorId; }

    public Integer getPredictionId() { return predictionId; }
    public void setPredictionId(Integer predictionId) { this.predictionId = predictionId; }

    public Timestamp getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(Timestamp inspectionDate) { this.inspectionDate = inspectionDate; }

    public InspectionDecision getFinalDecision() { return finalDecision; }
    public void setFinalDecision(InspectionDecision finalDecision) { this.finalDecision = finalDecision; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }

    public String getPredictedLabel() { return predictedLabel; }
    public void setPredictedLabel(String predictedLabel) { this.predictedLabel = predictedLabel; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return "Inspection #" + inspectionId + " [" + finalDecision + "] - " + productName;
    }
}
