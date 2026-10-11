package com.industrialinspection.service;

import com.industrialinspection.dao.QualityReportDAO;
import com.industrialinspection.dao.QualityReportDAOImpl;
import com.industrialinspection.exception.ReportGenerationException;
import com.industrialinspection.model.QualityReport;
import com.industrialinspection.model.enums.ReportType;
import com.industrialinspection.report.*;
import com.industrialinspection.util.ThreadPoolManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Quality Report Coordinator using Strategy & Factory design patterns.
 */
public class QualityReportService {

    private final Map<ReportType, ReportGenerator> generators = new EnumMap<>(ReportType.class);
    private final QualityReportDAO reportDAO;
    private static final String DEFAULT_REPORT_DIR = "reports/export";

    public QualityReportService() {
        this(new QualityReportDAOImpl());
    }

    public QualityReportService(QualityReportDAO reportDAO) {
        this.reportDAO = reportDAO;
        registerGenerators();
    }

    private void registerGenerators() {
        generators.put(ReportType.DAILY_INSPECTION, new DailyInspectionReportGenerator());
        generators.put(ReportType.BATCH_QUALITY, new BatchQualityReportGenerator());
        generators.put(ReportType.PRODUCT_DEFECT, new ProductDefectReportGenerator());
        generators.put(ReportType.OPERATOR_PERFORMANCE, new OperatorPerformanceReportGenerator());
        generators.put(ReportType.MACHINE_DEFECT, new MachineDefectReportGenerator());
        generators.put(ReportType.MONTHLY_SUMMARY, new MonthlyQualitySummaryReportGenerator());
        generators.put(ReportType.PRODUCTION_TREND, new ProductionTrendReportGenerator());
    }

    public QualityReport generateReport(ReportType type, String format, Integer userId, File customDestination) 
            throws ReportGenerationException {
        ReportGenerator generator = generators.get(type);
        if (generator == null) {
            throw new ReportGenerationException("No report generator registered for type: " + type);
        }

        File dest = customDestination;
        if (dest == null) {
            File dir = new File(DEFAULT_REPORT_DIR);
            if (!dir.exists()) dir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String ext = "csv".equalsIgnoreCase(format) ? "csv" : "txt";
            dest = new File(dir, type.name().toLowerCase() + "_" + timestamp + "." + ext);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("format", format);
        params.put("generatedAt", new Date());

        QualityReport report = generator.generate(params, dest, userId != null ? userId : 1);
        reportDAO.addReport(report);

        AuditService.getInstance().logAction(userId, "REPORT_GENERATED", "quality_reports", report.getReportId(),
                "Generated " + type.getTitle() + " (" + report.getFileFormat() + ")");

        return report;
    }

    public Future<QualityReport> generateReportAsync(ReportType type, String format, Integer userId, File dest) {
        return ThreadPoolManager.getInstance().submitImageTask(() -> generateReport(type, format, userId, dest));
    }

    public List<QualityReport> getAllGeneratedReports() {
        return reportDAO.getAllReports();
    }

    public QualityReport getReportById(int id) {
        return reportDAO.getReportById(id);
    }
}
