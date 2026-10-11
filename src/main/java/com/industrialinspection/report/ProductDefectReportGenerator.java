package com.industrialinspection.report;

import com.industrialinspection.dao.DefectResultDAO;
import com.industrialinspection.dao.DefectResultDAOImpl;
import com.industrialinspection.exception.ReportGenerationException;
import com.industrialinspection.model.DefectResult;
import com.industrialinspection.model.QualityReport;
import com.industrialinspection.model.enums.ReportType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ProductDefectReportGenerator implements ReportGenerator {

    private final DefectResultDAO defectResultDAO;

    public ProductDefectReportGenerator() {
        this(new DefectResultDAOImpl());
    }

    public ProductDefectReportGenerator(DefectResultDAO defectResultDAO) {
        this.defectResultDAO = defectResultDAO;
    }

    @Override
    public String getReportName() {
        return "Product Defect Analysis";
    }

    @Override
    public QualityReport generate(Map<String, Object> parameters, File destination, int generatedBy) 
            throws ReportGenerationException {
        try {
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            List<DefectResult> defects = defectResultDAO.getAllDefectResults();
            boolean isCsv = destination.getName().toLowerCase().endsWith(".csv");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
                if (isCsv) {
                    writer.write("ABC Precision Manufacturing Pvt. Ltd. - Product Defect Analysis\n");
                    writer.write("Generated:," + new Date() + "\n\n");
                    writer.write("Defect ID,Inspection ID,Defect Name,Severity,Location,Comments\n");
                    for (DefectResult d : defects) {
                        writer.write(String.format("%d,%d,\"%s\",%s,%s,\"%s\"\n",
                                d.getDefectResultId(),
                                d.getInspectionId(),
                                d.getDefectName() != null ? d.getDefectName() : ("Defect #" + d.getDefectTypeId()),
                                d.getSeverity().name(),
                                d.getLocation().name(),
                                d.getComments() != null ? d.getComments().replace("\"", "\"\"") : ""));
                    }
                } else {
                    writer.write("===============================================================================\n");
                    writer.write("                   ABC PRECISION MANUFACTURING PVT. LTD.                       \n");
                    writer.write("                        PRODUCT DEFECT TAXONOMY REPORT                         \n");
                    writer.write("===============================================================================\n");
                    writer.write("Generated:        " + new Date() + "\n");
                    writer.write("Total Defects:    " + defects.size() + "\n");
                    writer.write("-------------------------------------------------------------------------------\n");
                    writer.write(String.format("%-6s | %-8s | %-22s | %-10s | %-14s\n", "ID", "Insp #", "Defect Type", "Severity", "Location"));
                    writer.write("-------------------------------------------------------------------------------\n");
                    for (DefectResult d : defects) {
                        String name = d.getDefectName() != null ? d.getDefectName() : ("Defect #" + d.getDefectTypeId());
                        writer.write(String.format("%-6d | %-8d | %-22s | %-10s | %-14s\n",
                                d.getDefectResultId(), d.getInspectionId(), name, d.getSeverity(), d.getLocation()));
                    }
                    writer.write("===============================================================================\n");
                }
                writer.flush();
            }

            QualityReport qr = new QualityReport();
            qr.setReportType(ReportType.PRODUCT_DEFECT);
            qr.setTitle(getReportName());
            qr.setGeneratedBy(generatedBy);
            qr.setFilePath(destination.getPath().replace("\\", "/"));
            qr.setFileFormat(isCsv ? "CSV" : "TXT");
            qr.setGeneratedAt(new Timestamp(System.currentTimeMillis()));
            return qr;

        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate defect report: " + e.getMessage(), e);
        }
    }
}
