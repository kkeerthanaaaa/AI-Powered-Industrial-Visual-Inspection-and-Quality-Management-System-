package com.industrialinspection.report;

import com.industrialinspection.dao.InspectionDAO;
import com.industrialinspection.dao.InspectionDAOImpl;
import com.industrialinspection.exception.ReportGenerationException;
import com.industrialinspection.model.InspectionRecord;
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

public class DailyInspectionReportGenerator implements ReportGenerator {

    private final InspectionDAO inspectionDAO;

    public DailyInspectionReportGenerator() {
        this(new InspectionDAOImpl());
    }

    public DailyInspectionReportGenerator(InspectionDAO inspectionDAO) {
        this.inspectionDAO = inspectionDAO;
    }

    @Override
    public String getReportName() {
        return "Daily Inspection Report";
    }

    @Override
    public QualityReport generate(Map<String, Object> parameters, File destination, int generatedBy) 
            throws ReportGenerationException {
        try {
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            List<InspectionRecord> records = inspectionDAO.getAllInspections();
            boolean isCsv = destination.getName().toLowerCase().endsWith(".csv");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
                if (isCsv) {
                    writer.write("ABC Precision Manufacturing Pvt. Ltd. - Daily Inspection Report\n");
                    writer.write("Generated:," + new Date() + "\n");
                    writer.write("Total Records:," + records.size() + "\n\n");
                    writer.write("Inspection ID,Product,Batch Number,Inspector,Decision,Confidence,Date,Remarks\n");

                    for (InspectionRecord r : records) {
                        writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",%s,%.2f%%,\"%s\",\"%s\"\n",
                                r.getInspectionId(),
                                r.getProductName() != null ? r.getProductName() : "",
                                r.getBatchNumber() != null ? r.getBatchNumber() : "",
                                r.getInspectorName() != null ? r.getInspectorName() : "",
                                r.getFinalDecision().name(),
                                r.getConfidenceScore(),
                                DateTimeUtil.formatTimestamp(r.getInspectionDate()),
                                r.getRemarks() != null ? r.getRemarks().replace("\"", "\"\"") : ""));
                    }
                } else {
                    writer.write("===============================================================================\n");
                    writer.write("                   ABC PRECISION MANUFACTURING PVT. LTD.                       \n");
                    writer.write("                        DAILY VISUAL INSPECTION REPORT                         \n");
                    writer.write("===============================================================================\n");
                    writer.write("Report Title:     " + getReportName() + "\n");
                    writer.write("Generated At:     " + new Date() + "\n");
                    writer.write("Total Records:    " + records.size() + "\n");
                    writer.write("-------------------------------------------------------------------------------\n");
                    writer.write(String.format("%-6s | %-24s | %-16s | %-10s | %-8s\n", "ID", "Product", "Batch", "Decision", "Conf"));
                    writer.write("-------------------------------------------------------------------------------\n");

                    int passCount = 0, failCount = 0;
                    for (InspectionRecord r : records) {
                        String prod = r.getProductName() != null ? r.getProductName() : ("Prod#" + r.getProductId());
                        if (prod.length() > 24) prod = prod.substring(0, 21) + "...";
                        String batch = r.getBatchNumber() != null ? r.getBatchNumber() : ("B#" + r.getBatchId());

                        writer.write(String.format("%-6d | %-24s | %-16s | %-10s | %6.1f%%\n",
                                r.getInspectionId(), prod, batch, r.getFinalDecision().name(), r.getConfidenceScore()));

                        if (r.getFinalDecision().name().equals("PASS")) passCount++;
                        else if (r.getFinalDecision().name().equals("FAIL")) failCount++;
                    }

                    writer.write("-------------------------------------------------------------------------------\n");
                    writer.write("SUMMARY STATISTICS:\n");
                    writer.write("Total Inspected:  " + records.size() + "\n");
                    writer.write("Passed:           " + passCount + "\n");
                    writer.write("Failed / Scrapped:" + failCount + "\n");
                    writer.write("Pass Yield Rate:  " + (records.isEmpty() ? "0%" : String.format("%.2f%%", (passCount * 100.0 / records.size()))) + "\n");
                    writer.write("===============================================================================\n");
                }
                writer.flush();
            }

            QualityReport qr = new QualityReport();
            qr.setReportType(ReportType.DAILY_INSPECTION);
            qr.setTitle(getReportName());
            qr.setGeneratedBy(generatedBy);
            qr.setFilePath(destination.getPath().replace("\\", "/"));
            qr.setFileFormat(isCsv ? "CSV" : "TXT");
            qr.setGeneratedAt(new Timestamp(System.currentTimeMillis()));
            return qr;

        } catch (IOException e) {
            throw new ReportGenerationException("Failed to write daily inspection report: " + e.getMessage(), e);
        }
    }
}
