package com.industrialinspection.service;

import com.industrialinspection.dao.AuditLogDAO;
import com.industrialinspection.dao.AuditLogDAOImpl;
import com.industrialinspection.model.AuditLog;
import com.industrialinspection.util.ThreadPoolManager;

import java.util.List;

/**
 * Asynchronous Audit Logging Service using multithreaded worker executor.
 */
public class AuditService {

    private static volatile AuditService instance;
    private final AuditLogDAO auditLogDAO;

    private AuditService() {
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public static AuditService getInstance() {
        if (instance == null) {
            synchronized (AuditService.class) {
                if (instance == null) {
                    instance = new AuditService();
                }
            }
        }
        return instance;
    }

    public void logAction(Integer userId, String action, String entityName, Integer entityId, String description) {
        ThreadPoolManager.getInstance().executeAuditLog(() -> {
            try {
                AuditLog log = new AuditLog(userId, action, entityName, entityId, description);
                auditLogDAO.addLog(log);
            } catch (Exception e) {
                System.err.println("[AUDIT ERROR] Failed to record audit log: " + e.getMessage());
            }
        });
    }

    public List<AuditLog> getAllLogs() {
        return auditLogDAO.getAllLogs();
    }

    public List<AuditLog> getLogsByUserId(int userId) {
        return auditLogDAO.getLogsByUserId(userId);
    }
}
