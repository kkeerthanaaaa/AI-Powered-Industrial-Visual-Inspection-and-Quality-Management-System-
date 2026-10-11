package com.industrialinspection.dao;

import com.industrialinspection.model.Operator;
import com.industrialinspection.model.enums.OperatorStatus;
import com.industrialinspection.model.enums.Shift;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class OperatorDAOImpl implements OperatorDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<Integer, Operator> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        cache.put(1, new Operator(1, "Kavita Roy", "EMP-OP-101", "Quality Assurance", Shift.MORNING, "+91 98765 43210", OperatorStatus.ACTIVE));
        cache.put(2, new Operator(2, "Anand Verma", "EMP-OP-102", "Precision Machining", Shift.EVENING, "+91 98765 43211", OperatorStatus.ACTIVE));
        cache.put(3, new Operator(3, "Deepak Kumar", "EMP-OP-103", "Aerospace Assembly", Shift.NIGHT, "+91 98765 43212", OperatorStatus.ACTIVE));
        cache.put(4, new Operator(4, "Sunita Joshi", "EMP-OP-104", "Stamping & Forging", Shift.MORNING, "+91 98765 43213", OperatorStatus.ACTIVE));
    }

    @Override
    public void addOperator(Operator operator) {
        String sql = "INSERT INTO operators (name, employee_code, department, shift, contact, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, operator.getName());
            ps.setString(2, operator.getEmployeeCode());
            ps.setString(3, operator.getDepartment());
            ps.setString(4, operator.getShift().name());
            ps.setString(5, operator.getContact());
            ps.setString(6, operator.getStatus().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    operator.setOperatorId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            if (operator.getOperatorId() == 0) {
                operator.setOperatorId(idGen.incrementAndGet());
            }
        }
        cache.put(operator.getOperatorId(), operator);
    }

    @Override
    public Operator getOperatorById(int id) {
        String sql = "SELECT * FROM operators WHERE operator_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOperator(rs);
                }
            }
        } catch (Exception ignored) {}
        return cache.get(id);
    }

    @Override
    public Operator getOperatorByCode(String code) {
        String sql = "SELECT * FROM operators WHERE employee_code = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOperator(rs);
                }
            }
        } catch (Exception ignored) {}

        for (Operator o : cache.values()) {
            if (o.getEmployeeCode().equalsIgnoreCase(code)) return o;
        }
        return null;
    }

    @Override
    public List<Operator> getAllOperators() {
        List<Operator> list = new ArrayList<>();
        String sql = "SELECT * FROM operators ORDER BY operator_id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapOperator(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    @Override
    public void updateOperator(Operator operator) {
        String sql = "UPDATE operators SET name = ?, employee_code = ?, department = ?, shift = ?, contact = ?, status = ? WHERE operator_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, operator.getName());
            ps.setString(2, operator.getEmployeeCode());
            ps.setString(3, operator.getDepartment());
            ps.setString(4, operator.getShift().name());
            ps.setString(5, operator.getContact());
            ps.setString(6, operator.getStatus().name());
            ps.setInt(7, operator.getOperatorId());
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.put(operator.getOperatorId(), operator);
    }

    @Override
    public void deleteOperator(int id) {
        String sql = "DELETE FROM operators WHERE operator_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.remove(id);
    }

    private Operator mapOperator(ResultSet rs) throws SQLException {
        Operator o = new Operator();
        o.setOperatorId(rs.getInt("operator_id"));
        o.setName(rs.getString("name"));
        o.setEmployeeCode(rs.getString("employee_code"));
        o.setDepartment(rs.getString("department"));
        o.setShift(Shift.fromString(rs.getString("shift")));
        o.setContact(rs.getString("contact"));
        o.setStatus(OperatorStatus.fromString(rs.getString("status")));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        return o;
    }
}
