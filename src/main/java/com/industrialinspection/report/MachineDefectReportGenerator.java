package com.industrialinspection.report;

import com.industrialinspection.dao.MachineDAO;
import com.industrialinspection.dao.MachineDAOImpl;
import com.industrialinspection.exception.ReportGenerationException;
import com.industrialinspection.model.Machine;
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

public class MachineDefectReportGenerator implements ReportGenerator {

    private final MachineDAO machineDAO;

    public MachineDefectReportGenerator() {
        this(new MachineDAOImpl());
    }

    public MachineDefectReportGenerator(MachineDAO machineDAO) {
        this.machineDAO = machineDAO;
    }

    @Override
    public String getReportName() {
        return "Machine-wise Defect Analysis";
    }

    @Override
    public QualityReport generate(Map<String, Object> parameters, File destination, int generatedBy) 
            throws ReportGenerationException {
        try {
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            List<Machine> machines = machineDAO.getAllMachines();
            boolean isCsv = destination.getName().toLowerCase().endsWith(".csv");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
                if (isCsv) {
                    writer.write("ABC Precision Manufacturing Pvt. Ltd. - Machine Defect Analysis\n");
                    writer.write("Generated:," + new Date() + "\n\n");
                    writer.write("Machine Code,Machine Name,Line,Type,Status,Installation Date,Last Maintenance\n");
                    for (Machine m : machines) {
                        writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\"\n",
                                m.getMachineCode(), m.getMachineName(), m.getProductionLine(), m.getMachineType(),
                                m.getStatus().name(), DateTimeUtil.formatDate(m.getInstallationDate()),
                                DateTimeUtil.formatDate(m.getLastMaintenance())));
                    }
                } else {
                    writer.write("===============================================================================\n");
                    writer.write("                   ABC PRECISION MANUFACTURING PVT. LTD.                       \n");
                    writer.write("                         MACHINE HEALTH & DEFECT AUDIT                         \n");
                    writer.write("===============================================================================\n");
                    writer.write("Generated:        " + new Date() + "\n");
                    writer.write("Tracked Units:    " + machines.size() + "\n");
                    writer.write("-------------------------------------------------------------------------------\n");
                    writer.write(String.format("%-10s | %-24s | %-16s | %-12s\n", "Machine", "Line", "Status", "Maintenance"));
                    writer.write("-------------------------------------------------------------------------------\n");
                    for (Machine m : machines) {
                        writer.write(String.format("%-10s | %-24s | %-16s | %-12s\n",
                                m.getMachineCode(), m.getProductionLine(), m.getStatus(), DateTimeUtil.formatDate(m.getLastMaintenance())));
                    }
                    writer.write("===============================================================================\n");
                }
                writer.flush();
            }

            QualityReport qr = new QualityReport();
            qr.setReportType(ReportType.MACHINE_DEFECT);
            qr.setTitle(getReportName());
            qr.setGeneratedBy(generatedBy);
            qr.setFilePath(destination.getPath().replace("\\", "/"));
            qr.setFileFormat(isCsv ? "CSV" : "TXT");
            qr.setGeneratedAt(new Timestamp(System.currentTimeMillis()));
            return qr;

        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate machine defect report: " + e.getMessage(), e);
        }
    }
}
