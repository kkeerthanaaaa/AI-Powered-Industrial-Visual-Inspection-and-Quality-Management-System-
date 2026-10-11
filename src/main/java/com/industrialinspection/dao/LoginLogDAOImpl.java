package com.industrialinspection.dao;

import com.industrialinspection.model.LoginLog;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LoginLogDAOImpl implements LoginLogDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Deque<LoginLog> memoryLogs = new ConcurrentLinkedDeque<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    @Override
    public void addLoginAttempt(LoginLog log) {
        String sql = "INSERT INTO login_log (user_id, username_attempted, status, failure_reason, ip_address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() != null) {
                ps.setInt(1, log.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, log.getUsernameAttempted());
            ps.setString(3, log.getStatus());
            ps.setString(4, log.getFailureReason());
            ps.setString(5, log.getIpAddress());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) log.setLoginId(rs.getInt(1));
            }
        } catch (Exception e) {
            if (log.getLoginId() == 0) {
                log.setLoginId(idGen.incrementAndGet());
            }
        }
        if (log.getAttemptedAt() == null) {
            log.setAttemptedAt(new Timestamp(System.currentTimeMillis()));
        }
        memoryLogs.addFirst(log);
    }

    @Override
    public List<LoginLog> getAllLoginLogs() {
        List<LoginLog> list = new ArrayList<>();
        String sql = "SELECT * FROM login_log ORDER BY login_id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapLoginLog(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(memoryLogs);
    }

    @Override
    public List<LoginLog> getRecentFailedAttempts(String username, int minutes) {
        long thresholdTime = System.currentTimeMillis() - (minutes * 60 * 1000L);
        return getAllLoginLogs().stream()
                .filter(l -> "FAILED".equalsIgnoreCase(l.getStatus()) &&
                             l.getUsernameAttempted().equalsIgnoreCase(username) &&
                             l.getAttemptedAt() != null &&
                             l.getAttemptedAt().getTime() >= thresholdTime)
                .collect(Collectors.toList());
    }

    private LoginLog mapLoginLog(ResultSet rs) throws SQLException {
        LoginLog l = new LoginLog();
        l.setLoginId(rs.getInt("login_id"));
        int uid = rs.getInt("user_id");
        l.setUserId(rs.wasNull() ? null : uid);
        l.setUsernameAttempted(rs.getString("username_attempted"));
        l.setStatus(rs.getString("status"));
        l.setFailureReason(rs.getString("failure_reason"));
        l.setIpAddress(rs.getString("ip_address"));
        l.setAttemptedAt(rs.getTimestamp("attempted_at"));
        return l;
    }
}
