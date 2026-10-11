package com.industrialinspection.dao;

import com.industrialinspection.model.DefectType;
import com.industrialinspection.model.enums.SeverityLevel;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DefectTypeDAOImpl implements DefectTypeDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<Integer, DefectType> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        cache.put(1, new DefectType(1, "DEF-SCRATCH", "Surface Scratch", "Surface Flaw", SeverityLevel.MEDIUM,
                "Linear abrasive damage penetrating the surface coating or polished face", "Micro-buffing or recoat pass"));
        cache.put(2, new DefectType(2, "DEF-CRACK", "Structural Micro-Crack", "Structural Defect", SeverityLevel.CRITICAL,
                "Stress or fatigue fracture propagating across alloy body", "Immediate scrap and tool recalibration"));
        cache.put(3, new DefectType(3, "DEF-DENT", "Mechanical Dent", "Form Deformation", SeverityLevel.HIGH,
                "Depression caused by impact handling or clamping over-torque", "Dimensional re-measurement or rejection"));
        cache.put(4, new DefectType(4, "DEF-DISCOLOR", "Thermal Discoloration", "Surface Finish", SeverityLevel.LOW,
                "Oxidation discoloration due to excessive heat during machining cut", "Coolant flow adjustment and chemical rinse"));
        cache.put(5, new DefectType(5, "DEF-SURF-DMG", "Surface Damage & Pitting", "Surface Flaw", SeverityLevel.HIGH,
                "Localized casting porosity, pitting corrosion, or tool chatter scarring", "Material review and tooling replacement"));
        cache.put(6, new DefectType(6, "DEF-MISS-COMP", "Missing Component / Dowel", "Assembly Error", SeverityLevel.CRITICAL,
                "Absence of secondary fastener, sealing ring, or locator dowel pin", "Return to assembly station for component insertion"));
        cache.put(7, new DefectType(7, "DEF-CORROSION", "Chemical Corrosion / Rust", "Material Degradation", SeverityLevel.HIGH,
                "Oxidative corrosion visible on ground steel mating face", "Rust-inhibitor immersion and sand-blast refacing"));
        cache.put(8, new DefectType(8, "DEF-MISALIGN", "Feature Misalignment", "Dimensional Tolerance", SeverityLevel.MEDIUM,
                "Angular or concentric deviation exceeding GD&T boundary", "CNC offset recalibration"));
    }

    @Override
    public void addDefectType(DefectType defectType) {
        String sql = "INSERT INTO defect_types (defect_code, defect_name, category, default_severity, description, standard_remedy) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, defectType.getDefectCode());
            ps.setString(2, defectType.getDefectName());
            ps.setString(3, defectType.getCategory());
            ps.setString(4, defectType.getDefaultSeverity().name());
            ps.setString(5, defectType.getDescription());
            ps.setString(6, defectType.getStandardRemedy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) defectType.setDefectTypeId(rs.getInt(1));
            }
        } catch (Exception e) {
            if (defectType.getDefectTypeId() == 0) {
                defectType.setDefectTypeId(idGen.incrementAndGet());
            }
        }
        cache.put(defectType.getDefectTypeId(), defectType);
    }

    @Override
    public DefectType getDefectTypeById(int id) {
        String sql = "SELECT * FROM defect_types WHERE defect_type_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDefectType(rs);
            }
        } catch (Exception ignored) {}
        return cache.get(id);
    }

    @Override
    public DefectType getDefectTypeByCode(String code) {
        String sql = "SELECT * FROM defect_types WHERE defect_code = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDefectType(rs);
            }
        } catch (Exception ignored) {}

        for (DefectType dt : cache.values()) {
            if (dt.getDefectCode().equalsIgnoreCase(code)) return dt;
        }
        return null;
    }

    @Override
    public List<DefectType> getAllDefectTypes() {
        List<DefectType> list = new ArrayList<>();
        String sql = "SELECT * FROM defect_types ORDER BY defect_type_id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDefectType(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    @Override
    public void updateDefectType(DefectType defectType) {
        String sql = "UPDATE defect_types SET defect_code = ?, defect_name = ?, category = ?, default_severity = ?, description = ?, standard_remedy = ? WHERE defect_type_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, defectType.getDefectCode());
            ps.setString(2, defectType.getDefectName());
            ps.setString(3, defectType.getCategory());
            ps.setString(4, defectType.getDefaultSeverity().name());
            ps.setString(5, defectType.getDescription());
            ps.setString(6, defectType.getStandardRemedy());
            ps.setInt(7, defectType.getDefectTypeId());
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.put(defectType.getDefectTypeId(), defectType);
    }

    @Override
    public void deleteDefectType(int id) {
        String sql = "DELETE FROM defect_types WHERE defect_type_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.remove(id);
    }

    private DefectType mapDefectType(ResultSet rs) throws SQLException {
        DefectType dt = new DefectType();
        dt.setDefectTypeId(rs.getInt("defect_type_id"));
        dt.setDefectCode(rs.getString("defect_code"));
        dt.setDefectName(rs.getString("defect_name"));
        dt.setCategory(rs.getString("category"));
        dt.setDefaultSeverity(SeverityLevel.fromString(rs.getString("default_severity")));
        dt.setDescription(rs.getString("description"));
        dt.setStandardRemedy(rs.getString("standard_remedy"));
        dt.setCreatedAt(rs.getTimestamp("created_at"));
        return dt;
    }
}
