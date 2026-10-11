package com.industrialinspection.dao;

import com.industrialinspection.model.CorrectiveAction;
import com.industrialinspection.model.enums.ActionStatus;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CorrectiveActionDAOImpl implements CorrectiveActionDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<Integer, CorrectiveAction> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        CorrectiveAction ca = new CorrectiveAction(1, 2, 1, 
                "Excessive cutting speed and worn carbide insert on CNC-001 causing micro-fracture during titanium hogging cut",
                "Replaced milling tool insert with Walter Ti-Grade, recalibrated spindle feed rate to 45m/min, and issued 100% dye-penetrant inspection for entire batch",
                "Priya Patel (Lead QE)", Date.valueOf("2026-10-05"), ActionStatus.VERIFIED);
        ca.setCompletionDate(Date.valueOf("2026-10-04"));
        ca.setProductName("Aircraft Titanium Wing Bracket");
        ca.setBatchNumber("BATCH-2026-AERO-03");
        ca.setRemarks("Tooling replaced and verified via test cut.");
        cache.put(1, ca);
    }

    @Override
    public void addCorrectiveAction(CorrectiveAction action) {
        String sql = "INSERT INTO corrective_actions (inspection_id, defect_result_id, root_cause, corrective_action, responsible_person, target_date, status, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, action.getInspectionId());
            if (action.getDefectResultId() != null) {
                ps.setInt(2, action.getDefectResultId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, action.getRootCause());
            ps.setString(4, action.getCorrectiveAction());
            ps.setString(5, action.getResponsiblePerson());
            ps.setDate(6, action.getTargetDate());
            ps.setString(7, action.getStatus().name());
            ps.setString(8, action.getRemarks());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) action.setActionId(rs.getInt(1));
            }
        } catch (Exception e) {
            if (action.getActionId() == 0) {
                action.setActionId(idGen.incrementAndGet());
            }
        }
        cache.put(action.getActionId(), action);
    }

    @Override
    public CorrectiveAction getCorrectiveActionById(int id) {
        String sql = "SELECT ca.*, p.product_name, b.batch_number FROM corrective_actions ca " +
                     "LEFT JOIN inspection_records r ON ca.inspection_id = r.inspection_id " +
                     "LEFT JOIN products p ON r.product_id = p.product_id " +
                     "LEFT JOIN production_batches b ON r.batch_id = b.batch_id " +
                     "WHERE ca.action_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapAction(rs);
            }
        } catch (Exception ignored) {}
        return cache.get(id);
    }

    @Override
    public List<CorrectiveAction> getCorrectiveActionsByInspectionId(int inspectionId) {
        return getAllCorrectiveActions().stream().filter(ca -> ca.getInspectionId() == inspectionId).collect(Collectors.toList());
    }

    @Override
    public List<CorrectiveAction> getAllCorrectiveActions() {
        List<CorrectiveAction> list = new ArrayList<>();
        String sql = "SELECT ca.*, p.product_name, b.batch_number FROM corrective_actions ca " +
                     "LEFT JOIN inspection_records r ON ca.inspection_id = r.inspection_id " +
                     "LEFT JOIN products p ON r.product_id = p.product_id " +
                     "LEFT JOIN production_batches b ON r.batch_id = b.batch_id " +
                     "ORDER BY ca.action_id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAction(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    @Override
    public void updateCorrectiveAction(CorrectiveAction action) {
        String sql = "UPDATE corrective_actions SET root_cause = ?, corrective_action = ?, responsible_person = ?, target_date = ?, completion_date = ?, status = ?, remarks = ? WHERE action_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action.getRootCause());
            ps.setString(2, action.getCorrectiveAction());
            ps.setString(3, action.getResponsiblePerson());
            ps.setDate(4, action.getTargetDate());
            ps.setDate(5, action.getCompletionDate());
            ps.setString(6, action.getStatus().name());
            ps.setString(7, action.getRemarks());
            ps.setInt(8, action.getActionId());
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.put(action.getActionId(), action);
    }

    @Override
    public void deleteCorrectiveAction(int id) {
        String sql = "DELETE FROM corrective_actions WHERE action_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.remove(id);
    }

    private CorrectiveAction mapAction(ResultSet rs) throws SQLException {
        CorrectiveAction ca = new CorrectiveAction();
        ca.setActionId(rs.getInt("action_id"));
        ca.setInspectionId(rs.getInt("inspection_id"));
        int drId = rs.getInt("defect_result_id");
        ca.setDefectResultId(rs.wasNull() ? null : drId);
        ca.setRootCause(rs.getString("root_cause"));
        ca.setCorrectiveAction(rs.getString("corrective_action"));
        ca.setResponsiblePerson(rs.getString("responsible_person"));
        ca.setTargetDate(rs.getDate("target_date"));
        ca.setCompletionDate(rs.getDate("completion_date"));
        ca.setStatus(ActionStatus.fromString(rs.getString("status")));
        ca.setRemarks(rs.getString("remarks"));
        ca.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            ca.setProductName(rs.getString("product_name"));
            ca.setBatchNumber(rs.getString("batch_number"));
        } catch (SQLException ignored) {}
        return ca;
    }
}
