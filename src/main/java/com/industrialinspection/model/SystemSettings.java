package com.industrialinspection.model;

import java.sql.Timestamp;

public class SystemSettings {
    private int settingId;
    private String settingKey;
    private String settingValue;
    private String settingGroup;
    private String description;
    private Timestamp updatedAt;

    public SystemSettings() {
        this.settingGroup = "GENERAL";
    }

    public SystemSettings(int settingId, String settingKey, String settingValue, String settingGroup, String description) {
        this.settingId = settingId;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingGroup = settingGroup;
        this.description = description;
    }

    public int getSettingId() { return settingId; }
    public void setSettingId(int settingId) { this.settingId = settingId; }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getSettingGroup() { return settingGroup; }
    public void setSettingGroup(String settingGroup) { this.settingGroup = settingGroup; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return settingKey + " = " + settingValue;
    }
}
