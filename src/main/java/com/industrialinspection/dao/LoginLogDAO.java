package com.industrialinspection.dao;

import com.industrialinspection.model.LoginLog;
import java.util.List;

public interface LoginLogDAO {
    void addLoginAttempt(LoginLog log);
    List<LoginLog> getAllLoginLogs();
    List<LoginLog> getRecentFailedAttempts(String username, int minutes);
}
