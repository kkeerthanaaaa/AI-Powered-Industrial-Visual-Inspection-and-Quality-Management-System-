package com.industrialinspection.service;

import com.industrialinspection.dao.BatchDAO;
import com.industrialinspection.dao.BatchDAOImpl;
import com.industrialinspection.exception.BatchNotFoundException;
import com.industrialinspection.exception.ValidationException;
import com.industrialinspection.model.ProductionBatch;
import com.industrialinspection.model.enums.BatchStatus;
import com.industrialinspection.util.CSVUtil;
import com.industrialinspection.util.DateTimeUtil;
import com.industrialinspection.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class BatchService {

    private final BatchDAO batchDAO;

    public BatchService() {
        this(new BatchDAOImpl());
    }

    public BatchService(BatchDAO batchDAO) {
        this.batchDAO = batchDAO;
    }

    public void createBatch(ProductionBatch batch, Integer userId) throws ValidationException {
        ValidationUtil.validateNotEmpty(batch.getBatchNumber(), "Batch Number");
        ValidationUtil.validatePositive(batch.getProductId(), "Product Reference");
        ValidationUtil.validatePositive(batch.getBatchSize(), "Batch Size");

        if (batch.getProductionDate() == null) {
            batch.setProductionDate(new Date(System.currentTimeMillis()));
        }

        batchDAO.addBatch(batch);
        AuditService.getInstance().logAction(userId, "BATCH_CREATED", "production_batches", batch.getBatchId(),
                "Registered production batch: " + batch.getBatchNumber() + " (Size: " + batch.getBatchSize() + ")");
    }

    public void updateBatch(ProductionBatch batch, Integer userId) throws ValidationException, BatchNotFoundException {
        ValidationUtil.validateNotEmpty(batch.getBatchNumber(), "Batch Number");
        ValidationUtil.validatePositive(batch.getBatchSize(), "Batch Size");

        if (batchDAO.getBatchById(batch.getBatchId()) == null) {
            throw new BatchNotFoundException("Batch with ID " + batch.getBatchId() + " not found.");
        }

        batchDAO.updateBatch(batch);
        AuditService.getInstance().logAction(userId, "BATCH_UPDATED", "production_batches", batch.getBatchId(),
                "Updated production batch: " + batch.getBatchNumber());
    }

    public void deleteBatch(int batchId, Integer userId) throws BatchNotFoundException {
        ProductionBatch b = batchDAO.getBatchById(batchId);
        if (b == null) {
            throw new BatchNotFoundException("Batch with ID " + batchId + " not found.");
        }
        batchDAO.deleteBatch(batchId);
        AuditService.getInstance().logAction(userId, "BATCH_DELETED", "production_batches", batchId,
                "Deleted production batch: " + b.getBatchNumber());
    }

    public ProductionBatch getBatchById(int id) throws BatchNotFoundException {
        ProductionBatch b = batchDAO.getBatchById(id);
        if (b == null) {
            throw new BatchNotFoundException("Batch with ID " + id + " not found.");
        }
        return b;
    }

    public List<ProductionBatch> getAllBatches() {
        return batchDAO.getAllBatches();
    }

    public List<ProductionBatch> searchBatches(String keyword, String status) {
        return batchDAO.searchBatches(keyword, status);
    }

    public void updateBatchStatus(int batchId, BatchStatus status, Integer userId) {
        ProductionBatch b = batchDAO.getBatchById(batchId);
        if (b != null) {
            b.setStatus(status);
            batchDAO.updateBatch(b);
            AuditService.getInstance().logAction(userId, "BATCH_STATUS_CHANGED", "production_batches", batchId,
                    "Batch " + b.getBatchNumber() + " status transitioned to " + status);
        }
    }

    public void exportBatchesToCSV(File destination) throws IOException {
        List<ProductionBatch> batches = getAllBatches();
        List<String[]> csvData = new ArrayList<>();
        csvData.add(new String[]{"Batch ID", "Batch Number", "Product ID", "Batch Size", "Production Date", "Shift", "Machine ID", "Operator ID", "Status"});

        for (ProductionBatch b : batches) {
            csvData.add(new String[]{
                String.valueOf(b.getBatchId()),
                b.getBatchNumber(),
                String.valueOf(b.getProductId()),
                String.valueOf(b.getBatchSize()),
                DateTimeUtil.formatDate(b.getProductionDate()),
                b.getShift().name(),
                String.valueOf(b.getMachineId()),
                String.valueOf(b.getOperatorId()),
                b.getStatus().name()
            });
        }
        CSVUtil.writeCSV(destination, csvData);
    }

    public int importBatchesFromCSV(File sourceFile, Integer userId) throws IOException, ValidationException {
        List<String[]> rows = CSVUtil.readCSV(sourceFile);
        if (rows.isEmpty()) return 0;

        int importedCount = 0;
        int startIndex = 0;
        if (rows.get(0)[0].toLowerCase().contains("batch") || rows.get(0)[0].toLowerCase().contains("id") || rows.get(0)[0].toLowerCase().contains("number")) {
            startIndex = 1;
        }

        for (int i = startIndex; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length >= 8) {
                ProductionBatch b = new ProductionBatch();
                b.setBatchNumber(row[1]);
                try {
                    b.setProductId(Integer.parseInt(row[2].trim()));
                    b.setBatchSize(Integer.parseInt(row[3].trim()));
                    b.setProductionDate(DateTimeUtil.parseDate(row[4].trim()));
                    b.setShift(com.industrialinspection.model.enums.Shift.valueOf(row[5].trim().toUpperCase()));
                    b.setMachineId(Integer.parseInt(row[6].trim()));
                    b.setOperatorId(Integer.parseInt(row[7].trim()));
                    if (row.length > 8) {
                        b.setStatus(BatchStatus.valueOf(row[8].trim().toUpperCase()));
                    } else {
                        b.setStatus(BatchStatus.PENDING);
                    }
                    createBatch(b, userId);
                    importedCount++;
                } catch (Exception ex) {
                    System.err.println("[WARN] Skipping malformed batch row " + i + ": " + ex.getMessage());
                }
            }
        }
        AuditService.getInstance().logAction(userId, "CSV_BATCH_IMPORT", "production_batches", 0, "Imported " + importedCount + " batches from CSV");
        return importedCount;
    }
}
