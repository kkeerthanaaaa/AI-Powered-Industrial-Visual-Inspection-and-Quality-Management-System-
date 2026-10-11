package com.industrialinspection.model;

import java.sql.Timestamp;

public class LoginLog {
    private int loginId;
    private Integer userId;
    private String usernameAttempted;
    private String status;
    private String failureReason;
    private String ipAddress;
    private Timestamp attemptedAt;

    public LoginLog() {
        this.ipAddress = "127.0.0.1";
    }

    public LoginLog(Integer userId, String usernameAttempted, String status, String failureReason, String ipAddress) {
        this.userId = userId;
        this.usernameAttempted = usernameAttempted;
        this.status = status;
        this.failureReason = failureReason;
        this.ipAddress = ipAddress;
    }

    public int getLoginId() { return loginId; }
    public void setLoginId(int loginId) { this.loginId = loginId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsernameAttempted() { return usernameAttempted; }
    public void setUsernameAttempted(String usernameAttempted) { this.usernameAttempted = usernameAttempted; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Timestamp getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(Timestamp attemptedAt) { this.attemptedAt = attemptedAt; }

    @Override
    public String toString() {
        return attemptedAt + ": " + usernameAttempted + " -> " + status + (failureReason != null ? " (" + failureReason + ")" : "");
    }
}
