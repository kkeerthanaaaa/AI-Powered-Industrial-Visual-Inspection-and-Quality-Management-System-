package com.industrialinspection.dao;

import com.industrialinspection.model.AuditLog;
import java.util.List;

public interface AuditLogDAO {
    void addLog(AuditLog log);
    List<AuditLog> getAllLogs();
    List<AuditLog> getLogsByUserId(int userId);
    List<AuditLog> getLogsByEntity(String entityName, int entityId);
}
