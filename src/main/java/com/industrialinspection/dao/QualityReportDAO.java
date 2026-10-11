package com.industrialinspection.dao;

import com.industrialinspection.model.QualityReport;
import java.util.List;

public interface QualityReportDAO {
    void addReport(QualityReport report);
    QualityReport getReportById(int id);
    List<QualityReport> getAllReports();
    void deleteReport(int id);
}
