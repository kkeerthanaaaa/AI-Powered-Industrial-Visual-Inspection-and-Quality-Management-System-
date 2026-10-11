package com.industrialinspection.model;

import com.industrialinspection.model.enums.BatchStatus;
import com.industrialinspection.model.enums.Shift;
import java.sql.Date;
import java.sql.Timestamp;

public class ProductionBatch {
    private int batchId;
    private String batchNumber;
    private int productId;
    private int batchSize;
    private Date productionDate;
    private Shift shift;
    private int machineId;
    private int operatorId;
    private BatchStatus status;
    private Timestamp createdAt;

    // Associated navigation labels for display convenience
    private String productName;
    private String machineName;
    private String operatorName;

    public ProductionBatch() {
        this.shift = Shift.MORNING;
        this.status = BatchStatus.PENDING;
    }

    public ProductionBatch(int batchId, String batchNumber, int productId, int batchSize, 
                           Date productionDate, Shift shift, int machineId, int operatorId, BatchStatus status) {
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.productId = productId;
        this.batchSize = batchSize;
        this.productionDate = productionDate;
        this.shift = shift;
        this.machineId = machineId;
        this.operatorId = operatorId;
        this.status = status;
    }

    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public Date getProductionDate() { return productionDate; }
    public void setProductionDate(Date productionDate) { this.productionDate = productionDate; }

    public Shift getShift() { return shift; }
    public void setShift(Shift shift) { this.shift = shift; }

    public int getMachineId() { return machineId; }
    public void setMachineId(int machineId) { this.machineId = machineId; }

    public int getOperatorId() { return operatorId; }
    public void setOperatorId(int operatorId) { this.operatorId = operatorId; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    @Override
    public String toString() {
        return batchNumber + " (" + (productName != null ? productName : "Product #" + productId) + ")";
    }
}
