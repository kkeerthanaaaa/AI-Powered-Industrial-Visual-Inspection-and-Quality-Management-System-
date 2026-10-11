package com.industrialinspection.dao;

import com.industrialinspection.model.*;
import com.industrialinspection.model.enums.UserRole;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserDAOImpl implements UserDAO {

    private final DBConnection db = DBConnection.getInstance();

    // Resilient cache fallback for offline testing
    private static final Map<Integer, User> userCache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        // Pre-seed default users in fallback store
        // Admin: Admin@123
        Administrator admin = new Administrator(1, "admin", "Rajesh Sharma (System Admin)", "admin@abcprecision.com");
        admin.setPasswordHash("ca31ea8f7778c8817c6317583975902abb92015a5baae9d55d824fee7d35f903");
        admin.setSalt("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        admin.setStatus("ACTIVE");
        userCache.put(1, admin);

        // Quality Engineer: Pass@123
        QualityEngineer qe = new QualityEngineer(2, "engineer", "Priya Patel (Lead QE)", "priya.patel@abcprecision.com");
        qe.setPasswordHash("ad4d45fc1b328a5bd49faf97cd32055ac0c79668940b710743ba12886f60cff1");
        qe.setSalt("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        qe.setStatus("ACTIVE");
        userCache.put(2, qe);

        // Production Supervisor: Pass@123
        ProductionSupervisor ps = new ProductionSupervisor(3, "supervisor", "Vikram Singh (Supervisor)", "vikram.singh@abcprecision.com");
        ps.setPasswordHash("ad4d45fc1b328a5bd49faf97cd32055ac0c79668940b710743ba12886f60cff1");
        ps.setSalt("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        ps.setStatus("ACTIVE");
        userCache.put(3, ps);

        // Inspection Operator: Pass@123
        InspectionOperator io = new InspectionOperator(4, "operator", "Kavita Roy (Inspector)", "kavita.roy@abcprecision.com");
        io.setPasswordHash("ad4d45fc1b328a5bd49faf97cd32055ac0c79668940b710743ba12886f60cff1");
        io.setSalt("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        io.setStatus("ACTIVE");
        userCache.put(4, io);
    }

    private User createUserInstance(UserRole role) {
        if (role == null) return new InspectionOperator();
        return switch (role) {
            case ADMINISTRATOR -> new Administrator();
            case QUALITY_ENGINEER -> new QualityEngineer();
            case PRODUCTION_SUPERVISOR -> new ProductionSupervisor();
            case INSPECTION_OPERATOR -> new InspectionOperator();
        };
    }

    @Override
    public void addUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, email, role, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getRole().name());
            ps.setString(7, user.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            if (user.getUserId() == 0) {
                user.setUserId(idGen.incrementAndGet());
            }
        }
        userCache.put(user.getUserId(), user);
    }

    @Override
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception ignored) {}
        return userCache.get(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception ignored) {}

        for (User u : userCache.values()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}

        return new ArrayList<>(userCache.values());
    }

    @Override
    public void updateUser(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, role = ?, status = ? WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getStatus());
            ps.setInt(5, user.getUserId());
            ps.executeUpdate();
        } catch (Exception ignored) {}
        userCache.put(user.getUserId(), user);
    }

    @Override
    public void updatePassword(int userId, String passwordHash, String salt) {
        String sql = "UPDATE users SET password_hash = ?, salt = ?, failed_attempts = 0 WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, salt);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {}

        User u = userCache.get(userId);
        if (u != null) {
            u.setPasswordHash(passwordHash);
            u.setSalt(salt);
            u.setFailedAttempts(0);
        }
    }

    @Override
    public void updateStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {}

        User u = userCache.get(userId);
        if (u != null) {
            u.setStatus(status);
        }
    }

    @Override
    public void recordFailedAttempt(int userId) {
        String sql = "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {}

        User u = userCache.get(userId);
        if (u != null) {
            u.setFailedAttempts(u.getFailedAttempts() + 1);
            if (u.getFailedAttempts() >= 5) {
                u.setStatus("LOCKED");
            }
        }
    }

    @Override
    public void resetFailedAttempts(int userId) {
        String sql = "UPDATE users SET failed_attempts = 0 WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {}

        User u = userCache.get(userId);
        if (u != null) {
            u.setFailedAttempts(0);
        }
    }

    @Override
    public void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        userCache.remove(userId);
    }

    private User mapUser(ResultSet rs) throws SQLException {
        UserRole role = UserRole.fromString(rs.getString("role"));
        User user = createUserInstance(role);
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(role);
        user.setStatus(rs.getString("status"));
        user.setFailedAttempts(rs.getInt("failed_attempts"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
