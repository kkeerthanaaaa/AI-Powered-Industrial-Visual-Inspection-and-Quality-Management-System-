package com.industrialinspection.service;

import com.industrialinspection.dao.*;
import com.industrialinspection.exception.InspectionException;
import com.industrialinspection.exception.ValidationException;
import com.industrialinspection.model.InspectionRecord;
import com.industrialinspection.model.enums.BatchStatus;
import com.industrialinspection.model.enums.InspectionDecision;
import com.industrialinspection.util.CSVUtil;
import com.industrialinspection.util.DateTimeUtil;
import com.industrialinspection.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class InspectionService {

    private final InspectionDAO inspectionDAO;
    private final ProductImageDAO imageDAO;
    private final BatchDAO batchDAO;

    public InspectionService() {
        this(new InspectionDAOImpl(), new ProductImageDAOImpl(), new BatchDAOImpl());
    }

    public InspectionService(InspectionDAO inspectionDAO, ProductImageDAO imageDAO, BatchDAO batchDAO) {
        this.inspectionDAO = inspectionDAO;
        this.imageDAO = imageDAO;
        this.batchDAO = batchDAO;
    }

    public InspectionRecord completeInspection(int productId, int batchId, int imageId, int inspectorId,
                                              Integer predictionId, InspectionDecision decision, 
                                              String remarks, String status) 
            throws ValidationException, InspectionException {
        ValidationUtil.validatePositive(productId, "Product ID");
        ValidationUtil.validatePositive(batchId, "Batch ID");
        ValidationUtil.validatePositive(imageId, "Image ID");
        ValidationUtil.validatePositive(inspectorId, "Inspector ID");

        if (decision == null) {
            decision = InspectionDecision.REQUIRES_REVIEW;
        }

        InspectionRecord record = new InspectionRecord();
        record.setProductId(productId);
        record.setBatchId(batchId);
        record.setImageId(imageId);
        record.setInspectorId(inspectorId);
        record.setPredictionId(predictionId);
        record.setInspectionDate(new Timestamp(System.currentTimeMillis()));
        record.setFinalDecision(decision);
        record.setRemarks(remarks != null ? remarks : "");
        record.setStatus(status != null ? status : "SUBMITTED");

        inspectionDAO.addInspection(record);

        // Update optical image status
        imageDAO.updateInspectionStatus(imageId, "INSPECTED");

        // If inspection failed, update batch status to REJECTED or QUARANTINED
        if (decision == InspectionDecision.FAIL) {
            var batch = batchDAO.getBatchById(batchId);
            if (batch != null && batch.getStatus() != BatchStatus.APPROVED) {
                batch.setStatus(BatchStatus.REJECTED);
                batchDAO.updateBatch(batch);
            }
        }

        AuditService.getInstance().logAction(inspectorId, "INSPECTION_CREATED", "inspection_records", record.getInspectionId(),
                "Inspection completed with decision [" + decision + "] for Batch #" + batchId + " (Product #" + productId + ")");

        return record;
    }

    public InspectionRecord getInspectionById(int id) {
        return inspectionDAO.getInspectionById(id);
    }

    public List<InspectionRecord> getAllInspections() {
        return inspectionDAO.getAllInspections();
    }

    public List<InspectionRecord> getInspectionsByBatchId(int batchId) {
        return inspectionDAO.getInspectionsByBatchId(batchId);
    }

    public List<InspectionRecord> searchInspections(String keyword, String decision) {
        return inspectionDAO.searchInspections(keyword, decision);
    }

    public void updateInspectionDecision(int inspectionId, InspectionDecision decision, String remarks, int auditorId) {
        InspectionRecord r = inspectionDAO.getInspectionById(inspectionId);
        if (r != null) {
            r.setFinalDecision(decision);
            r.setRemarks(remarks);
            r.setStatus("AUDITED");
            inspectionDAO.updateInspection(r);
            AuditService.getInstance().logAction(auditorId, "INSPECTION_AUDITED", "inspection_records", inspectionId,
                    "Quality Engineer updated inspection decision to [" + decision + "]");
        }
    }

    public void exportInspectionsToCSV(File destination) throws IOException {
        List<InspectionRecord> list = getAllInspections();
        List<String[]> csv = new ArrayList<>();
        csv.add(new String[]{"Inspection ID", "Product", "Batch Number", "Inspector", "AI Label", "Confidence", "Final Decision", "Date", "Remarks", "Status"});

        for (InspectionRecord r : list) {
            csv.add(new String[]{
                String.valueOf(r.getInspectionId()),
                r.getProductName() != null ? r.getProductName() : ("Product #" + r.getProductId()),
                r.getBatchNumber() != null ? r.getBatchNumber() : ("Batch #" + r.getBatchId()),
                r.getInspectorName() != null ? r.getInspectorName() : ("User #" + r.getInspectorId()),
                r.getPredictedLabel() != null ? r.getPredictedLabel() : "N/A",
                String.format("%.1f%%", r.getConfidenceScore()),
                r.getFinalDecision().name(),
                DateTimeUtil.formatTimestamp(r.getInspectionDate()),
                r.getRemarks(),
                r.getStatus()
            });
        }
        CSVUtil.writeCSV(destination, csv);
    }
}
