package com.industrialinspection.dao;

import com.industrialinspection.model.QualityReport;
import com.industrialinspection.model.enums.ReportType;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class QualityReportDAOImpl implements QualityReportDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<Integer, QualityReport> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    @Override
    public void addReport(QualityReport report) {
        String sql = "INSERT INTO quality_reports (report_type, title, generated_by, parameters_json, file_path, file_format) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, report.getReportType().name());
            ps.setString(2, report.getTitle());
            ps.setInt(3, report.getGeneratedBy());
            ps.setString(4, report.getParametersJson());
            ps.setString(5, report.getFilePath());
            ps.setString(6, report.getFileFormat());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) report.setReportId(rs.getInt(1));
            }
        } catch (Exception e) {
            if (report.getReportId() == 0) {
                report.setReportId(idGen.incrementAndGet());
            }
        }
        if (report.getGeneratedAt() == null) {
            report.setGeneratedAt(new Timestamp(System.currentTimeMillis()));
        }
        cache.put(report.getReportId(), report);
    }

    @Override
    public QualityReport getReportById(int id) {
        String sql = "SELECT qr.*, u.full_name AS generated_by_name FROM quality_reports qr " +
                     "LEFT JOIN users u ON qr.generated_by = u.user_id " +
                     "WHERE qr.report_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapReport(rs);
            }
        } catch (Exception ignored) {}
        return cache.get(id);
    }

    @Override
    public List<QualityReport> getAllReports() {
        List<QualityReport> list = new ArrayList<>();
        String sql = "SELECT qr.*, u.full_name AS generated_by_name FROM quality_reports qr " +
                     "LEFT JOIN users u ON qr.generated_by = u.user_id " +
                     "ORDER BY qr.report_id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapReport(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    @Override
    public void deleteReport(int id) {
        String sql = "DELETE FROM quality_reports WHERE report_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.remove(id);
    }

    private QualityReport mapReport(ResultSet rs) throws SQLException {
        QualityReport qr = new QualityReport();
        qr.setReportId(rs.getInt("report_id"));
        qr.setReportType(ReportType.fromString(rs.getString("report_type")));
        qr.setTitle(rs.getString("title"));
        qr.setGeneratedBy(rs.getInt("generated_by"));
        qr.setParametersJson(rs.getString("parameters_json"));
        qr.setFilePath(rs.getString("file_path"));
        qr.setFileFormat(rs.getString("file_format"));
        qr.setGeneratedAt(rs.getTimestamp("generated_at"));
        try {
            qr.setGeneratedByName(rs.getString("generated_by_name"));
        } catch (SQLException ignored) {}
        return qr;
    }
}
