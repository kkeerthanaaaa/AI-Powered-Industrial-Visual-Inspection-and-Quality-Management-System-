package com.industrialinspection.dao;

import com.industrialinspection.model.SystemSettings;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SystemSettingsDAOImpl implements SystemSettingsDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<String, SystemSettings> cache = new ConcurrentHashMap<>();

    static {
        cache.put("COMPANY_NAME", new SystemSettings(1, "COMPANY_NAME", "ABC Precision Manufacturing Pvt. Ltd.", "GENERAL", "Company registered name"));
        cache.put("COMPANY_FACILITY", new SystemSettings(2, "COMPANY_FACILITY", "Plant 3, Aerospace & Precision Machining Hub, Pune, India", "GENERAL", "Plant facility description"));
        cache.put("AI_CONFIDENCE_THRESHOLD", new SystemSettings(3, "AI_CONFIDENCE_THRESHOLD", "85.0", "INSPECTION", "Minimum AI confidence percentage to recommend automatic pass"));
        cache.put("MAX_IMAGE_UPLOAD_MB", new SystemSettings(4, "MAX_IMAGE_UPLOAD_MB", "25", "INSPECTION", "Maximum inspection file size limit in megabytes"));
        cache.put("SUPPORTED_EXTENSIONS", new SystemSettings(5, "SUPPORTED_EXTENSIONS", "jpg,jpeg,png,bmp", "INSPECTION", "Permitted optical inspection image formats"));
        cache.put("CAMERA_PRIMARY_ID", new SystemSettings(6, "CAMERA_PRIMARY_ID", "CAM-OPTICAL-01", "HARDWARE", "Default camera mount identifier in line inspection cell"));
        cache.put("AUTO_AUDIT_LOGGING", new SystemSettings(7, "AUTO_AUDIT_LOGGING", "true", "SECURITY", "Flag to toggle background multithreaded audit logging"));
        cache.put("DEFAULT_BACKUP_PATH", new SystemSettings(8, "DEFAULT_BACKUP_PATH", "backups", "DATABASE", "Default folder for SQL database dumps"));
    }

    @Override
    public void setSetting(String key, String value, String group, String description) {
        String sql = "INSERT INTO system_settings (setting_key, setting_value, setting_group, description) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = ?, setting_group = ?, description = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, group);
            ps.setString(4, description);
            ps.setString(5, value);
            ps.setString(6, group);
            ps.setString(7, description);
            ps.executeUpdate();
        } catch (Exception ignored) {}

        SystemSettings s = cache.get(key);
        if (s == null) {
            s = new SystemSettings(cache.size() + 1, key, value, group, description);
            cache.put(key, s);
        } else {
            s.setSettingValue(value);
            s.setSettingGroup(group);
            s.setDescription(description);
        }
    }

    @Override
    public String getSettingValue(String key, String defaultValue) {
        SystemSettings s = getSetting(key);
        return (s != null && s.getSettingValue() != null) ? s.getSettingValue() : defaultValue;
    }

    @Override
    public SystemSettings getSetting(String key) {
        String sql = "SELECT * FROM system_settings WHERE setting_key = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSetting(rs);
                }
            }
        } catch (Exception ignored) {}
        return cache.get(key);
    }

    @Override
    public List<SystemSettings> getAllSettings() {
        List<SystemSettings> list = new ArrayList<>();
        String sql = "SELECT * FROM system_settings ORDER BY setting_group ASC, setting_key ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapSetting(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    private SystemSettings mapSetting(ResultSet rs) throws SQLException {
        SystemSettings s = new SystemSettings();
        s.setSettingId(rs.getInt("setting_id"));
        s.setSettingKey(rs.getString("setting_key"));
        s.setSettingValue(rs.getString("setting_value"));
        s.setSettingGroup(rs.getString("setting_group"));
        s.setDescription(rs.getString("description"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));
        return s;
    }
}
