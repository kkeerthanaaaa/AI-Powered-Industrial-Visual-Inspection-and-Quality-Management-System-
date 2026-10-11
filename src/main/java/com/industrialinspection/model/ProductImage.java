package com.industrialinspection.model;

import java.sql.Timestamp;

public class ProductImage {
    private int imageId;
    private int productId;
    private int batchId;
    private String fileName;
    private String filePath;
    private Timestamp captureDate;
    private String cameraId;
    private int width;
    private int height;
    private long fileSizeBytes;
    private String inspectionStatus;
    private Timestamp createdAt;

    // Helpers
    private String productName;
    private String batchNumber;

    public ProductImage() {
        this.cameraId = "CAM-OPTICAL-01";
        this.inspectionStatus = "UNINSPECTED";
    }

    public ProductImage(int imageId, int productId, int batchId, String fileName, 
                        String filePath, int width, int height, long fileSizeBytes) {
        this();
        this.imageId = imageId;
        this.productId = productId;
        this.batchId = batchId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.width = width;
        this.height = height;
        this.fileSizeBytes = fileSizeBytes;
    }

    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Timestamp getCaptureDate() { return captureDate; }
    public void setCaptureDate(Timestamp captureDate) { this.captureDate = captureDate; }

    public String getCameraId() { return cameraId; }
    public void setCameraId(String cameraId) { this.cameraId = cameraId; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getInspectionStatus() { return inspectionStatus; }
    public void setInspectionStatus(String inspectionStatus) { this.inspectionStatus = inspectionStatus; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getResolutionString() {
        return width + " x " + height + " px";
    }

    public String getFormattedSize() {
        if (fileSizeBytes < 1024) return fileSizeBytes + " B";
        if (fileSizeBytes < 1024 * 1024) return String.format("%.1f KB", fileSizeBytes / 1024.0);
        return String.format("%.2f MB", fileSizeBytes / (1024.0 * 1024.0));
    }

    @Override
    public String toString() {
        return fileName + " (" + getResolutionString() + ")";
    }
}
