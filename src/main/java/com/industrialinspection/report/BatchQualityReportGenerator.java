package com.industrialinspection.report;

import com.industrialinspection.dao.BatchDAO;
import com.industrialinspection.dao.BatchDAOImpl;
import com.industrialinspection.exception.ReportGenerationException;
import com.industrialinspection.model.ProductionBatch;
import com.industrialinspection.model.QualityReport;
import com.industrialinspection.model.enums.ReportType;
import com.industrialinspection.util.DateTimeUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class BatchQualityReportGenerator implements ReportGenerator {

    private final BatchDAO batchDAO;

    public BatchQualityReportGenerator() {
        this(new BatchDAOImpl());
    }

    public BatchQualityReportGenerator(BatchDAO batchDAO) {
        this.batchDAO = batchDAO;
    }

    @Override
    public String getReportName() {
        return "Batch Quality Report";
    }

    @Override
    public QualityReport generate(Map<String, Object> parameters, File destination, int generatedBy) 
            throws ReportGenerationException {
        try {
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            List<ProductionBatch> batches = batchDAO.getAllBatches();
            boolean isCsv = destination.getName().toLowerCase().endsWith(".csv");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
                if (isCsv) {
                    writer.write("ABC Precision Manufacturing Pvt. Ltd. - Batch Quality Report\n");
                    writer.write("Generated:," + new Date() + "\n");
                    writer.write("Batch Count:," + batches.size() + "\n\n");
                    writer.write("Batch Number,Product,Lot Size,Production Date,Shift,Machine,Operator,Status\n");
                    for (ProductionBatch b : batches) {
                        writer.write(String.format("\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",%s\n",
                                b.getBatchNumber(),
                                b.getProductName() != null ? b.getProductName() : "",
                                b.getBatchSize(),
                                DateTimeUtil.formatDate(b.getProductionDate()),
                                b.getShift().name(),
                                b.getMachineName() != null ? b.getMachineName() : "",
                                b.getOperatorName() != null ? b.getOperatorName() : "",
                                b.getStatus().name()));
                    }
                } else {
                    writer.write("===============================================================================\n");
                    writer.write("                   ABC PRECISION MANUFACTURING PVT. LTD.                       \n");
                    writer.write("                           BATCH QUALITY AUDIT REPORT                          \n");
                    writer.write("===============================================================================\n");
                    writer.write("Generated:        " + new Date() + "\n");
                    writer.write("Active Batches:   " + batches.size() + "\n");
                    writer.write("-------------------------------------------------------------------------------\n");
                    writer.write(String.format("%-18s | %-24s | %-8s | %-12s | %-10s\n", "Batch #", "Product", "Size", "Date", "Status"));
                    writer.write("-------------------------------------------------------------------------------\n");
                    for (ProductionBatch b : batches) {
                        String prod = b.getProductName() != null ? b.getProductName() : ("P#" + b.getProductId());
                        if (prod.length() > 24) prod = prod.substring(0, 21) + "...";
                        writer.write(String.format("%-18s | %-24s | %-8d | %-12s | %-10s\n",
                                b.getBatchNumber(), prod, b.getBatchSize(), DateTimeUtil.formatDate(b.getProductionDate()), b.getStatus()));
                    }
                    writer.write("===============================================================================\n");
                }
                writer.flush();
            }

            QualityReport qr = new QualityReport();
            qr.setReportType(ReportType.BATCH_QUALITY);
            qr.setTitle(getReportName());
            qr.setGeneratedBy(generatedBy);
            qr.setFilePath(destination.getPath().replace("\\", "/"));
            qr.setFileFormat(isCsv ? "CSV" : "TXT");
            qr.setGeneratedAt(new Timestamp(System.currentTimeMillis()));
            return qr;

        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate batch quality report: " + e.getMessage(), e);
        }
    }
}
