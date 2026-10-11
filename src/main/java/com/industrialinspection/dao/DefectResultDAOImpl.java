package com.industrialinspection.dao;

import com.industrialinspection.model.DefectResult;
import com.industrialinspection.model.enums.DefectLocation;
import com.industrialinspection.model.enums.SeverityLevel;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DefectResultDAOImpl implements DefectResultDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<Integer, DefectResult> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        DefectResult dr1 = new DefectResult(1, 2, 2, SeverityLevel.CRITICAL, DefectLocation.TOP_RIGHT, "Crack exceeds allowable 0.05mm ultrasonic limit");
        dr1.setDefectCode("DEF-CRACK");
        dr1.setDefectName("Structural Micro-Crack");
        dr1.setDefectX(1350);
        dr1.setDefectY(280);
        dr1.setDefectW(320);
        dr1.setDefectH(190);
        cache.put(1, dr1);
    }

    @Override
    public void addDefectResult(DefectResult defectResult) {
        String sql = "INSERT INTO defect_results (inspection_id, defect_type_id, severity, location, defect_x, defect_y, defect_w, defect_h, comments) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, defectResult.getInspectionId());
            ps.setInt(2, defectResult.getDefectTypeId());
            ps.setString(3, defectResult.getSeverity().name());
            ps.setString(4, defectResult.getLocation().name());
            ps.setInt(5, defectResult.getDefectX());
            ps.setInt(6, defectResult.getDefectY());
            ps.setInt(7, defectResult.getDefectW());
            ps.setInt(8, defectResult.getDefectH());
            ps.setString(9, defectResult.getComments());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) defectResult.setDefectResultId(rs.getInt(1));
            }
        } catch (Exception e) {
            if (defectResult.getDefectResultId() == 0) {
                defectResult.setDefectResultId(idGen.incrementAndGet());
            }
        }
        cache.put(defectResult.getDefectResultId(), defectResult);
    }

    @Override
    public DefectResult getDefectResultById(int id) {
        String sql = "SELECT dr.*, dt.defect_code, dt.defect_name " +
                     "FROM defect_results dr " +
                     "LEFT JOIN defect_types dt ON dr.defect_type_id = dt.defect_type_id " +
                     "WHERE dr.defect_result_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDefectResult(rs);
            }
        } catch (Exception ignored) {}
        return cache.get(id);
    }

    @Override
    public List<DefectResult> getDefectResultsByInspectionId(int inspectionId) {
        return getAllDefectResults().stream().filter(dr -> dr.getInspectionId() == inspectionId).collect(Collectors.toList());
    }

    @Override
    public List<DefectResult> getAllDefectResults() {
        List<DefectResult> list = new ArrayList<>();
        String sql = "SELECT dr.*, dt.defect_code, dt.defect_name " +
                     "FROM defect_results dr " +
                     "LEFT JOIN defect_types dt ON dr.defect_type_id = dt.defect_type_id " +
                     "ORDER BY dr.defect_result_id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDefectResult(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    @Override
    public void deleteDefectResult(int id) {
        String sql = "DELETE FROM defect_results WHERE defect_result_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.remove(id);
    }

    private DefectResult mapDefectResult(ResultSet rs) throws SQLException {
        DefectResult dr = new DefectResult();
        dr.setDefectResultId(rs.getInt("defect_result_id"));
        dr.setInspectionId(rs.getInt("inspection_id"));
        dr.setDefectTypeId(rs.getInt("defect_type_id"));
        dr.setSeverity(SeverityLevel.fromString(rs.getString("severity")));
        dr.setLocation(DefectLocation.fromString(rs.getString("location")));
        dr.setDefectX(rs.getInt("defect_x"));
        dr.setDefectY(rs.getInt("defect_y"));
        dr.setDefectW(rs.getInt("defect_w"));
        dr.setDefectH(rs.getInt("defect_h"));
        dr.setComments(rs.getString("comments"));
        dr.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            dr.setDefectCode(rs.getString("defect_code"));
            dr.setDefectName(rs.getString("defect_name"));
        } catch (SQLException ignored) {}
        return dr;
    }
}
