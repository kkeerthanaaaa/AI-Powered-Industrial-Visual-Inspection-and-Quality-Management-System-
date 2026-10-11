package com.industrialinspection.report;

import com.industrialinspection.dao.InspectionDAO;
import com.industrialinspection.dao.InspectionDAOImpl;
import com.industrialinspection.exception.ReportGenerationException;
import com.industrialinspection.model.QualityReport;
import com.industrialinspection.model.enums.ReportType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class MonthlyQualitySummaryReportGenerator implements ReportGenerator {

    private final InspectionDAO inspectionDAO;

    public MonthlyQualitySummaryReportGenerator() {
        this(new InspectionDAOImpl());
    }

    public MonthlyQualitySummaryReportGenerator(InspectionDAO inspectionDAO) {
        this.inspectionDAO = inspectionDAO;
    }

    @Override
    public String getReportName() {
        return "Monthly Quality Summary";
    }

    @Override
    public QualityReport generate(Map<String, Object> parameters, File destination, int generatedBy) 
            throws ReportGenerationException {
        try {
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            int total = inspectionDAO.getTotalInspectionsCount();
            int passed = inspectionDAO.getPassedInspectionsCount();
            int failed = inspectionDAO.getFailedInspectionsCount();
            int pending = inspectionDAO.getPendingReviewCount();
            double yield = total > 0 ? (passed * 100.0 / total) : 100.0;

            boolean isCsv = destination.getName().toLowerCase().endsWith(".csv");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
                if (isCsv) {
                    writer.write("ABC Precision Manufacturing Pvt. Ltd. - Monthly Quality Summary\n");
                    writer.write("Period:," + new Date() + "\n\n");
                    writer.write("Metric,Value\n");
                    writer.write("Total Optical Inspections," + total + "\n");
                    writer.write("Passed Inspections," + passed + "\n");
                    writer.write("Failed (Defective) Components," + failed + "\n");
                    writer.write("Pending Verification," + pending + "\n");
                    writer.write(String.format("First-Time Quality Yield,%.2f%%\n", yield));
                } else {
                    writer.write("===============================================================================\n");
                    writer.write("                   ABC PRECISION MANUFACTURING PVT. LTD.                       \n");
                    writer.write("                        MONTHLY QUALITY EXECUTIVE SUMMARY                      \n");
                    writer.write("===============================================================================\n");
                    writer.write("Audit Period:     Current Month (" + new Date() + ")\n");
                    writer.write("-------------------------------------------------------------------------------\n");
                    writer.write(String.format("Total Parts Inspected:   %d units\n", total));
                    writer.write(String.format("Parts Conforming (PASS): %d units\n", passed));
                    writer.write(String.format("Parts Rejected (FAIL):   %d units\n", failed));
                    writer.write(String.format("Under Secondary Review:  %d units\n", pending));
                    writer.write(String.format("First-Pass Quality Yield:%.2f%%\n", yield));
                    writer.write("-------------------------------------------------------------------------------\n");
                    writer.write("Standard: ISO 9001:2015 & AS9100D Quality Management Conformance Verified\n");
                    writer.write("===============================================================================\n");
                }
                writer.flush();
            }

            QualityReport qr = new QualityReport();
            qr.setReportType(ReportType.MONTHLY_SUMMARY);
            qr.setTitle(getReportName());
            qr.setGeneratedBy(generatedBy);
            qr.setFilePath(destination.getPath().replace("\\", "/"));
            qr.setFileFormat(isCsv ? "CSV" : "TXT");
            qr.setGeneratedAt(new Timestamp(System.currentTimeMillis()));
            return qr;

        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate monthly summary report: " + e.getMessage(), e);
        }
    }
}
