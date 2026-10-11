package com.industrialinspection.dao;

import com.industrialinspection.model.Machine;
import com.industrialinspection.model.enums.MachineStatus;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MachineDAOImpl implements MachineDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<Integer, Machine> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        Machine m1 = new Machine(1, "CNC-001", "DMG Mori 5-Axis Milling Rig", "CNC Milling", "Aero-Line-1", MachineStatus.ACTIVE, Date.valueOf("2023-01-15"));
        m1.setLastMaintenance(Date.valueOf("2026-09-10"));
        cache.put(1, m1);

        Machine m2 = new Machine(2, "CNC-002", "Mazak Integrex Multi-Tasking Center", "CNC Lathe & Mill", "Auto-Line-2", MachineStatus.ACTIVE, Date.valueOf("2023-04-20"));
        m2.setLastMaintenance(Date.valueOf("2026-09-15"));
        cache.put(2, m2);

        Machine m3 = new Machine(3, "PRESS-001", "Schuler 400T Hydraulic Stamping Press", "Stamping Press", "Chassis-Line-1", MachineStatus.ACTIVE, Date.valueOf("2022-11-05"));
        m3.setLastMaintenance(Date.valueOf("2026-08-25"));
        cache.put(3, m3);

        Machine m4 = new Machine(4, "ASSEMBLY-001", "KUKA Robotic Precision Cell", "Robotic Assembly", "Assembly-Line-3", MachineStatus.MAINTENANCE, Date.valueOf("2024-02-12"));
        m4.setLastMaintenance(Date.valueOf("2026-10-01"));
        cache.put(4, m4);
    }

    @Override
    public void addMachine(Machine machine) {
        String sql = "INSERT INTO machines (machine_code, machine_name, machine_type, production_line, status, installation_date, last_maintenance) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, machine.getMachineCode());
            ps.setString(2, machine.getMachineName());
            ps.setString(3, machine.getMachineType());
            ps.setString(4, machine.getProductionLine());
            ps.setString(5, machine.getStatus().name());
            ps.setDate(6, machine.getInstallationDate());
            ps.setDate(7, machine.getLastMaintenance());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    machine.setMachineId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            if (machine.getMachineId() == 0) {
                machine.setMachineId(idGen.incrementAndGet());
            }
        }
        cache.put(machine.getMachineId(), machine);
    }

    @Override
    public Machine getMachineById(int id) {
        String sql = "SELECT * FROM machines WHERE machine_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMachine(rs);
                }
            }
        } catch (Exception ignored) {}
        return cache.get(id);
    }

    @Override
    public Machine getMachineByCode(String code) {
        String sql = "SELECT * FROM machines WHERE machine_code = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMachine(rs);
                }
            }
        } catch (Exception ignored) {}

        for (Machine m : cache.values()) {
            if (m.getMachineCode().equalsIgnoreCase(code)) return m;
        }
        return null;
    }

    @Override
    public List<Machine> getAllMachines() {
        List<Machine> list = new ArrayList<>();
        String sql = "SELECT * FROM machines ORDER BY machine_id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapMachine(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    @Override
    public void updateMachine(Machine machine) {
        String sql = "UPDATE machines SET machine_code = ?, machine_name = ?, machine_type = ?, production_line = ?, status = ?, installation_date = ?, last_maintenance = ? WHERE machine_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, machine.getMachineCode());
            ps.setString(2, machine.getMachineName());
            ps.setString(3, machine.getMachineType());
            ps.setString(4, machine.getProductionLine());
            ps.setString(5, machine.getStatus().name());
            ps.setDate(6, machine.getInstallationDate());
            ps.setDate(7, machine.getLastMaintenance());
            ps.setInt(8, machine.getMachineId());
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.put(machine.getMachineId(), machine);
    }

    @Override
    public void deleteMachine(int id) {
        String sql = "DELETE FROM machines WHERE machine_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.remove(id);
    }

    private Machine mapMachine(ResultSet rs) throws SQLException {
        Machine m = new Machine();
        m.setMachineId(rs.getInt("machine_id"));
        m.setMachineCode(rs.getString("machine_code"));
        m.setMachineName(rs.getString("machine_name"));
        m.setMachineType(rs.getString("machine_type"));
        m.setProductionLine(rs.getString("production_line"));
        m.setStatus(MachineStatus.fromString(rs.getString("status")));
        m.setInstallationDate(rs.getDate("installation_date"));
        m.setLastMaintenance(rs.getDate("last_maintenance"));
        m.setCreatedAt(rs.getTimestamp("created_at"));
        return m;
    }
}
