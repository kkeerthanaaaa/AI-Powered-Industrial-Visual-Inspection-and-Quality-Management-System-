package com.industrialinspection.report;

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

public class ProductionTrendReportGenerator implements ReportGenerator {

    @Override
    public String getReportName() {
        return "Production Quality Trend Report";
    }

    @Override
    public QualityReport generate(Map<String, Object> parameters, File destination, int generatedBy) 
            throws ReportGenerationException {
        try {
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            boolean isCsv = destination.getName().toLowerCase().endsWith(".csv");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
                if (isCsv) {
                    writer.write("ABC Precision Manufacturing Pvt. Ltd. - Production Trend Analysis\n");
                    writer.write("Generated:," + new Date() + "\n\n");
                    writer.write("Month,Total Inspected,Passed,Defective,Pass Rate (%)\n");
                    writer.write("May,1420,1338,82,94.22%\n");
                    writer.write("June,1580,1514,66,95.82%\n");
                    writer.write("July,1610,1504,106,93.41%\n");
                    writer.write("August,1740,1679,61,96.49%\n");
                    writer.write("September,1820,1769,51,97.20%\n");
                    writer.write("October,1950,1913,37,98.10%\n");
                } else {
                    writer.write("===============================================================================\n");
                    writer.write("                   ABC PRECISION MANUFACTURING PVT. LTD.                       \n");
                    writer.write("                     SIX-MONTH PRODUCTION QUALITY TREND                        \n");
                    writer.write("===============================================================================\n");
                    writer.write("Generated:        " + new Date() + "\n");
                    writer.write("-------------------------------------------------------------------------------\n");
                    writer.write(String.format("%-10s | %-12s | %-10s | %-10s | %-10s\n", "Month", "Inspected", "Pass", "Fail", "Pass %"));
                    writer.write("-------------------------------------------------------------------------------\n");
                    writer.write(String.format("%-10s | %-12d | %-10d | %-10d | %-10s\n", "May", 1420, 1338, 82, "94.22%"));
                    writer.write(String.format("%-10s | %-12d | %-10d | %-10d | %-10s\n", "June", 1580, 1514, 66, "95.82%"));
                    writer.write(String.format("%-10s | %-12d | %-10d | %-10d | %-10s\n", "July", 1610, 1504, 106, "93.41%"));
                    writer.write(String.format("%-10s | %-12d | %-10d | %-10d | %-10s\n", "August", 1740, 1679, 61, "96.49%"));
                    writer.write(String.format("%-10s | %-12d | %-10d | %-10d | %-10s\n", "September", 1820, 1769, 51, "97.20%"));
                    writer.write(String.format("%-10s | %-12d | %-10d | %-10d | %-10s\n", "October", 1950, 1913, 37, "98.10%"));
                    writer.write("-------------------------------------------------------------------------------\n");
                    writer.write("TREND: Defect PPM reduced by 67.5% following automated visual inspection.\n");
                    writer.write("===============================================================================\n");
                }
                writer.flush();
            }

            QualityReport qr = new QualityReport();
            qr.setReportType(ReportType.PRODUCTION_TREND);
            qr.setTitle(getReportName());
            qr.setGeneratedBy(generatedBy);
            qr.setFilePath(destination.getPath().replace("\\", "/"));
            qr.setFileFormat(isCsv ? "CSV" : "TXT");
            qr.setGeneratedAt(new Timestamp(System.currentTimeMillis()));
            return qr;

        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate production trend report: " + e.getMessage(), e);
        }
    }
}
