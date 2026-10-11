package com.industrialinspection.dao;

import com.industrialinspection.model.AuditLog;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AuditLogDAOImpl implements AuditLogDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Deque<AuditLog> memoryLogs = new ConcurrentLinkedDeque<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        AuditLog al1 = new AuditLog(1, "USER_LOGIN", "users", 1, "Admin logged in from local workstation");
        al1.setLogId(1);
        al1.setCreatedAt(new Timestamp(System.currentTimeMillis() - 7200000));
        al1.setUsername("admin");
        memoryLogs.add(al1);

        AuditLog al2 = new AuditLog(2, "INSPECTION_CREATED", "inspection_records", 2, "Inspection completed for Aircraft Titanium Bracket BATCH-2026-AERO-03 (FAIL)");
        al2.setLogId(2);
        al2.setCreatedAt(new Timestamp(System.currentTimeMillis() - 3600000));
        al2.setUsername("engineer");
        memoryLogs.add(al2);
    }

    @Override
    public void addLog(AuditLog log) {
        String sql = "INSERT INTO audit_log (user_id, action, entity_name, entity_id, description, ip_address) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() != null) {
                ps.setInt(1, log.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntityName());
            if (log.getEntityId() != null) {
                ps.setInt(4, log.getEntityId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, log.getDescription());
            ps.setString(6, log.getIpAddress());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) log.setLogId(rs.getInt(1));
            }
        } catch (Exception e) {
            if (log.getLogId() == 0) {
                log.setLogId(idGen.incrementAndGet());
            }
        }
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        memoryLogs.addFirst(log);
    }

    @Override
    public List<AuditLog> getAllLogs() {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT a.*, u.username FROM audit_log a LEFT JOIN users u ON a.user_id = u.user_id ORDER BY a.log_id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapLog(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(memoryLogs);
    }

    @Override
    public List<AuditLog> getLogsByUserId(int userId) {
        return getAllLogs().stream().filter(l -> l.getUserId() != null && l.getUserId() == userId).collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> getLogsByEntity(String entityName, int entityId) {
        return getAllLogs().stream().filter(l -> entityName.equalsIgnoreCase(l.getEntityName()) && l.getEntityId() != null && l.getEntityId() == entityId).collect(Collectors.toList());
    }

    private AuditLog mapLog(ResultSet rs) throws SQLException {
        AuditLog l = new AuditLog();
        l.setLogId(rs.getInt("log_id"));
        int uid = rs.getInt("user_id");
        l.setUserId(rs.wasNull() ? null : uid);
        l.setAction(rs.getString("action"));
        l.setEntityName(rs.getString("entity_name"));
        int eid = rs.getInt("entity_id");
        l.setEntityId(rs.wasNull() ? null : eid);
        l.setDescription(rs.getString("description"));
        l.setIpAddress(rs.getString("ip_address"));
        l.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            l.setUsername(rs.getString("username"));
        } catch (SQLException ignored) {}
        return l;
    }
}
