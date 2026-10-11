package com.industrialinspection.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Product {
    private int productId;
    private String productName;
    private String category;
    private String productionLine;
    private Date manufacturingDate;
    private String batchNumber;
    private String supplier;
    private String customer;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Product() {}

    public Product(int productId, String productName, String category, String productionLine, 
                   Date manufacturingDate, String batchNumber, String supplier, String customer) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.productionLine = productionLine;
        this.manufacturingDate = manufacturingDate;
        this.batchNumber = batchNumber;
        this.supplier = supplier;
        this.customer = customer;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getProductionLine() { return productionLine; }
    public void setProductionLine(String productionLine) { this.productionLine = productionLine; }

    public Date getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(Date manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return productName + " [" + category + "]";
    }
}
