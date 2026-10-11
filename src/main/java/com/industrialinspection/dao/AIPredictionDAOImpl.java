package com.industrialinspection.dao;

import com.industrialinspection.model.AIPrediction;
import com.industrialinspection.model.enums.DefectLocation;
import com.industrialinspection.model.enums.SeverityLevel;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AIPredictionDAOImpl implements AIPredictionDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<Integer, AIPrediction> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        AIPrediction p1 = new AIPrediction(1, 1, "DEFECT_FREE", 98.45, "DEFECT_FREE", SeverityLevel.LOW, DefectLocation.CENTER, 0, 0, 0, 0, 42);
        cache.put(1, p1);

        AIPrediction p2 = new AIPrediction(2, 2, "CRACK", 94.60, "DEF-CRACK", SeverityLevel.CRITICAL, DefectLocation.TOP_RIGHT, 1350, 280, 320, 190, 58);
        cache.put(2, p2);

        AIPrediction p3 = new AIPrediction(3, 3, "DEFECT_FREE", 97.10, "DEFECT_FREE", SeverityLevel.LOW, DefectLocation.CENTER, 0, 0, 0, 0, 39);
        cache.put(3, p3);
    }

    @Override
    public void addPrediction(AIPrediction prediction) {
        String sql = "INSERT INTO ai_predictions (image_id, predicted_label, confidence_score, defect_type, severity, defect_location, bounding_box_x, bounding_box_y, bounding_box_w, bounding_box_h, processing_time_ms, raw_response_json) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, prediction.getImageId());
            ps.setString(2, prediction.getPredictedLabel());
            ps.setDouble(3, prediction.getConfidenceScore());
            ps.setString(4, prediction.getDefectType());
            ps.setString(5, prediction.getSeverity().name());
            ps.setString(6, prediction.getDefectLocation().name());
            ps.setInt(7, prediction.getBoundingBoxX());
            ps.setInt(8, prediction.getBoundingBoxY());
            ps.setInt(9, prediction.getBoundingBoxW());
            ps.setInt(10, prediction.getBoundingBoxH());
            ps.setInt(11, prediction.getProcessingTimeMs());
            ps.setString(12, prediction.getRawResponseJson());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    prediction.setPredictionId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            if (prediction.getPredictionId() == 0) {
                prediction.setPredictionId(idGen.incrementAndGet());
            }
        }
        cache.put(prediction.getPredictionId(), prediction);
    }

    @Override
    public AIPrediction getPredictionById(int id) {
        String sql = "SELECT * FROM ai_predictions WHERE prediction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPrediction(rs);
            }
        } catch (Exception ignored) {}
        return cache.get(id);
    }

    @Override
    public AIPrediction getPredictionByImageId(int imageId) {
        String sql = "SELECT * FROM ai_predictions WHERE image_id = ? ORDER BY prediction_id DESC LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, imageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPrediction(rs);
            }
        } catch (Exception ignored) {}

        for (AIPrediction p : cache.values()) {
            if (p.getImageId() == imageId) return p;
        }
        return null;
    }

    @Override
    public List<AIPrediction> getAllPredictions() {
        List<AIPrediction> list = new ArrayList<>();
        String sql = "SELECT * FROM ai_predictions ORDER BY prediction_id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPrediction(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    @Override
    public void deletePrediction(int id) {
        String sql = "DELETE FROM ai_predictions WHERE prediction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.remove(id);
    }

    private AIPrediction mapPrediction(ResultSet rs) throws SQLException {
        AIPrediction p = new AIPrediction();
        p.setPredictionId(rs.getInt("prediction_id"));
        p.setImageId(rs.getInt("image_id"));
        p.setPredictedLabel(rs.getString("predicted_label"));
        p.setConfidenceScore(rs.getDouble("confidence_score"));
        p.setDefectType(rs.getString("defect_type"));
        p.setSeverity(SeverityLevel.fromString(rs.getString("severity")));
        p.setDefectLocation(DefectLocation.fromString(rs.getString("defect_location")));
        p.setBoundingBoxX(rs.getInt("bounding_box_x"));
        p.setBoundingBoxY(rs.getInt("bounding_box_y"));
        p.setBoundingBoxW(rs.getInt("bounding_box_w"));
        p.setBoundingBoxH(rs.getInt("bounding_box_h"));
        p.setProcessingTimeMs(rs.getInt("processing_time_ms"));
        p.setRawResponseJson(rs.getString("raw_response_json"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }
}
