package com.industrialinspection.dao;

import com.industrialinspection.model.InspectionRecord;
import com.industrialinspection.model.enums.InspectionDecision;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InspectionDAOImpl implements InspectionDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<Integer, InspectionRecord> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        // Pre-seed sample inspections matching database script
        InspectionRecord r1 = new InspectionRecord(1, 1, 1, 1, 2, 1, InspectionDecision.PASS, "Finished rotor surface meets ISO 9001 specs. Balanced and scratch-free.");
        r1.setProductName("Brake Disc Ventilated 320mm");
        r1.setBatchNumber("BATCH-2026-BRK-01");
        r1.setInspectorName("Priya Patel (Lead QE)");
        r1.setPredictedLabel("DEFECT_FREE");
        r1.setConfidenceScore(98.45);
        r1.setImagePath("uploads/inspections/sample_brake_disc_inspected.png");
        r1.setStatus("AUDITED");
        cache.put(1, r1);

        InspectionRecord r2 = new InspectionRecord(2, 3, 3, 2, 2, 2, InspectionDecision.FAIL, "AI flagged 94.6% confidence structural micro-crack on upper spar mount. Scrap confirmed.");
        r2.setProductName("Aircraft Titanium Wing Bracket");
        r2.setBatchNumber("BATCH-2026-AERO-03");
        r2.setInspectorName("Priya Patel (Lead QE)");
        r2.setPredictedLabel("CRACK");
        r2.setConfidenceScore(94.60);
        r2.setImagePath("uploads/inspections/sample_aero_bracket_crack.png");
        r2.setStatus("AUDITED");
        cache.put(2, r2);

        InspectionRecord r3 = new InspectionRecord(3, 2, 2, 3, 4, 3, InspectionDecision.PASS, "Damping rubber bonding intact, bolt channels clean.");
        r3.setProductName("Engine Mount Hydraulic Assembly");
        r3.setBatchNumber("BATCH-2026-ENG-02");
        r3.setInspectorName("Kavita Roy (Inspector)");
        r3.setPredictedLabel("DEFECT_FREE");
        r3.setConfidenceScore(97.10);
        r3.setImagePath("uploads/inspections/sample_engine_mount_good.png");
        r3.setStatus("VERIFIED");
        cache.put(3, r3);
    }

    @Override
    public void addInspection(InspectionRecord record) {
        String sql = "INSERT INTO inspection_records (product_id, batch_id, image_id, inspector_id, prediction_id, final_decision, remarks, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getProductId());
            ps.setInt(2, record.getBatchId());
            ps.setInt(3, record.getImageId());
            ps.setInt(4, record.getInspectorId());
            if (record.getPredictionId() != null) {
                ps.setInt(5, record.getPredictionId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, record.getFinalDecision().name());
            ps.setString(7, record.getRemarks());
            ps.setString(8, record.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    record.setInspectionId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            if (record.getInspectionId() == 0) {
                record.setInspectionId(idGen.incrementAndGet());
            }
        }
        cache.put(record.getInspectionId(), record);
    }

    @Override
    public InspectionRecord getInspectionById(int id) {
        String sql = "SELECT r.*, p.product_name, b.batch_number, u.full_name AS inspector_name, " +
                     "pred.predicted_label, pred.confidence_score, img.file_path " +
                     "FROM inspection_records r " +
                     "LEFT JOIN products p ON r.product_id = p.product_id " +
                     "LEFT JOIN production_batches b ON r.batch_id = b.batch_id " +
                     "LEFT JOIN users u ON r.inspector_id = u.user_id " +
                     "LEFT JOIN ai_predictions pred ON r.prediction_id = pred.prediction_id " +
                     "LEFT JOIN product_images img ON r.image_id = img.image_id " +
                     "WHERE r.inspection_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapInspection(rs);
            }
        } catch (Exception ignored) {}
        return cache.get(id);
    }

    @Override
    public List<InspectionRecord> getAllInspections() {
        List<InspectionRecord> list = new ArrayList<>();
        String sql = "SELECT r.*, p.product_name, b.batch_number, u.full_name AS inspector_name, " +
                     "pred.predicted_label, pred.confidence_score, img.file_path " +
                     "FROM inspection_records r " +
                     "LEFT JOIN products p ON r.product_id = p.product_id " +
                     "LEFT JOIN production_batches b ON r.batch_id = b.batch_id " +
                     "LEFT JOIN users u ON r.inspector_id = u.user_id " +
                     "LEFT JOIN ai_predictions pred ON r.prediction_id = pred.prediction_id " +
                     "LEFT JOIN product_images img ON r.image_id = img.image_id " +
                     "ORDER BY r.inspection_id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapInspection(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    @Override
    public List<InspectionRecord> getInspectionsByBatchId(int batchId) {
        return getAllInspections().stream().filter(r -> r.getBatchId() == batchId).collect(Collectors.toList());
    }

    @Override
    public List<InspectionRecord> getInspectionsByProductId(int productId) {
        return getAllInspections().stream().filter(r -> r.getProductId() == productId).collect(Collectors.toList());
    }

    @Override
    public void updateInspection(InspectionRecord record) {
        String sql = "UPDATE inspection_records SET final_decision = ?, remarks = ?, status = ? WHERE inspection_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getFinalDecision().name());
            ps.setString(2, record.getRemarks());
            ps.setString(3, record.getStatus());
            ps.setInt(4, record.getInspectionId());
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.put(record.getInspectionId(), record);
    }

    @Override
    public void deleteInspection(int id) {
        String sql = "DELETE FROM inspection_records WHERE inspection_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.remove(id);
    }

    @Override
    public List<InspectionRecord> searchInspections(String keyword, String decision) {
        return getAllInspections().stream()
                .filter(r -> (keyword == null || keyword.isEmpty() ||
                              (r.getProductName() != null && r.getProductName().toLowerCase().contains(keyword.toLowerCase())) ||
                              (r.getBatchNumber() != null && r.getBatchNumber().toLowerCase().contains(keyword.toLowerCase())) ||
                              (r.getRemarks() != null && r.getRemarks().toLowerCase().contains(keyword.toLowerCase()))))
                .filter(r -> (decision == null || decision.isEmpty() || "ALL".equalsIgnoreCase(decision) ||
                              r.getFinalDecision().name().equalsIgnoreCase(decision)))
                .collect(Collectors.toList());
    }

    @Override
    public int getTotalInspectionsCount() {
        return getAllInspections().size();
    }

    @Override
    public int getPassedInspectionsCount() {
        return (int) getAllInspections().stream().filter(r -> r.getFinalDecision() == InspectionDecision.PASS).count();
    }

    @Override
    public int getFailedInspectionsCount() {
        return (int) getAllInspections().stream().filter(r -> r.getFinalDecision() == InspectionDecision.FAIL).count();
    }

    @Override
    public int getPendingReviewCount() {
        return (int) getAllInspections().stream().filter(r -> r.getFinalDecision() == InspectionDecision.REQUIRES_REVIEW).count();
    }

    private InspectionRecord mapInspection(ResultSet rs) throws SQLException {
        InspectionRecord r = new InspectionRecord();
        r.setInspectionId(rs.getInt("inspection_id"));
        r.setProductId(rs.getInt("product_id"));
        r.setBatchId(rs.getInt("batch_id"));
        r.setImageId(rs.getInt("image_id"));
        r.setInspectorId(rs.getInt("inspector_id"));
        int predId = rs.getInt("prediction_id");
        r.setPredictionId(rs.wasNull() ? null : predId);
        r.setInspectionDate(rs.getTimestamp("inspection_date"));
        r.setFinalDecision(InspectionDecision.fromString(rs.getString("final_decision")));
        r.setRemarks(rs.getString("remarks"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            r.setProductName(rs.getString("product_name"));
            r.setBatchNumber(rs.getString("batch_number"));
            r.setInspectorName(rs.getString("inspector_name"));
            r.setPredictedLabel(rs.getString("predicted_label"));
            r.setConfidenceScore(rs.getDouble("confidence_score"));
            r.setImagePath(rs.getString("file_path"));
        } catch (SQLException ignored) {}
        return r;
    }
}
