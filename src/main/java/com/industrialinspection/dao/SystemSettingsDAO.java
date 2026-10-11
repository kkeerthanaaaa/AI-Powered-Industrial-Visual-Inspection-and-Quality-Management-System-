package com.industrialinspection.dao;

import com.industrialinspection.model.SystemSettings;
import java.util.List;

public interface SystemSettingsDAO {
    void setSetting(String key, String value, String group, String description);
    String getSettingValue(String key, String defaultValue);
    SystemSettings getSetting(String key);
    List<SystemSettings> getAllSettings();
}
