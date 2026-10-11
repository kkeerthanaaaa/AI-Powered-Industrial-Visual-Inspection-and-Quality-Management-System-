package com.industrialinspection.report;

import com.industrialinspection.dao.OperatorDAO;
import com.industrialinspection.dao.OperatorDAOImpl;
import com.industrialinspection.exception.ReportGenerationException;
import com.industrialinspection.model.Operator;
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

public class OperatorPerformanceReportGenerator implements ReportGenerator {

    private final OperatorDAO operatorDAO;

    public OperatorPerformanceReportGenerator() {
        this(new OperatorDAOImpl());
    }

    public OperatorPerformanceReportGenerator(OperatorDAO operatorDAO) {
        this.operatorDAO = operatorDAO;
    }

    @Override
    public String getReportName() {
        return "Operator Performance Report";
    }

    @Override
    public QualityReport generate(Map<String, Object> parameters, File destination, int generatedBy) 
            throws ReportGenerationException {
        try {
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            List<Operator> operators = operatorDAO.getAllOperators();
            boolean isCsv = destination.getName().toLowerCase().endsWith(".csv");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
                if (isCsv) {
                    writer.write("ABC Precision Manufacturing Pvt. Ltd. - Operator Performance Report\n");
                    writer.write("Generated:," + new Date() + "\n\n");
                    writer.write("Operator ID,Name,Employee Code,Department,Shift,Contact,Status\n");
                    for (Operator o : operators) {
                        writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",%s,\"%s\",%s\n",
                                o.getOperatorId(), o.getName(), o.getEmployeeCode(), o.getDepartment(),
                                o.getShift().name(), o.getContact(), o.getStatus().name()));
                    }
                } else {
                    writer.write("===============================================================================\n");
                    writer.write("                   ABC PRECISION MANUFACTURING PVT. LTD.                       \n");
                    writer.write("                       OPERATOR AUDIT & PERFORMANCE LOG                        \n");
                    writer.write("===============================================================================\n");
                    writer.write("Generated:        " + new Date() + "\n");
                    writer.write("Active Operators: " + operators.size() + "\n");
                    writer.write("-------------------------------------------------------------------------------\n");
                    writer.write(String.format("%-10s | %-20s | %-20s | %-10s\n", "Emp Code", "Name", "Department", "Shift"));
                    writer.write("-------------------------------------------------------------------------------\n");
                    for (Operator o : operators) {
                        writer.write(String.format("%-10s | %-20s | %-20s | %-10s\n",
                                o.getEmployeeCode(), o.getName(), o.getDepartment(), o.getShift()));
                    }
                    writer.write("===============================================================================\n");
                }
                writer.flush();
            }

            QualityReport qr = new QualityReport();
            qr.setReportType(ReportType.OPERATOR_PERFORMANCE);
            qr.setTitle(getReportName());
            qr.setGeneratedBy(generatedBy);
            qr.setFilePath(destination.getPath().replace("\\", "/"));
            qr.setFileFormat(isCsv ? "CSV" : "TXT");
            qr.setGeneratedAt(new Timestamp(System.currentTimeMillis()));
            return qr;

        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate operator performance report: " + e.getMessage(), e);
        }
    }
}
