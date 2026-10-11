package com.industrialinspection.service;

import com.industrialinspection.dao.LoginLogDAO;
import com.industrialinspection.dao.LoginLogDAOImpl;
import com.industrialinspection.dao.UserDAO;
import com.industrialinspection.dao.UserDAOImpl;
import com.industrialinspection.exception.InvalidLoginException;
import com.industrialinspection.model.LoginLog;
import com.industrialinspection.model.User;
import com.industrialinspection.util.PasswordUtil;

/**
 * Enterprise Authentication Service implementing PBKDF2 verification,
 * brute-force lockout, and login auditing.
 */
public class AuthenticationService {

    private final UserDAO userDAO;
    private final LoginLogDAO loginLogDAO;
    private static User currentLoggedInUser = null;

    public AuthenticationService() {
        this(new UserDAOImpl(), new LoginLogDAOImpl());
    }

    public AuthenticationService(UserDAO userDAO, LoginLogDAO loginLogDAO) {
        this.userDAO = userDAO;
        this.loginLogDAO = loginLogDAO;
    }

    public static User getCurrentUser() {
        return currentLoggedInUser;
    }

    public static void setCurrentUser(User user) {
        currentLoggedInUser = user;
    }

    public User login(String username, String password, String ipAddress) throws InvalidLoginException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new InvalidLoginException("Username and password cannot be empty.");
        }

        User user = userDAO.getUserByUsername(username.trim());
        if (user == null) {
            loginLogDAO.addLoginAttempt(new LoginLog(null, username, "FAILED", "User not found", ipAddress));
            throw new InvalidLoginException("Invalid username or password.");
        }

        if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            loginLogDAO.addLoginAttempt(new LoginLog(user.getUserId(), username, "FAILED", "Account locked", ipAddress));
            throw new InvalidLoginException("Account is locked due to multiple failed login attempts. Contact Administrator.");
        }

        if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
            loginLogDAO.addLoginAttempt(new LoginLog(user.getUserId(), username, "FAILED", "Account inactive", ipAddress));
            throw new InvalidLoginException("Account is deactivated. Contact Administrator.");
        }

        boolean valid = PasswordUtil.verifyPassword(password, user.getPasswordHash(), user.getSalt());
        if (!valid) {
            userDAO.recordFailedAttempt(user.getUserId());
            loginLogDAO.addLoginAttempt(new LoginLog(user.getUserId(), username, "FAILED", "Incorrect password", ipAddress));
            throw new InvalidLoginException("Invalid username or password.");
        }

        // Login successful
        userDAO.resetFailedAttempts(user.getUserId());
        loginLogDAO.addLoginAttempt(new LoginLog(user.getUserId(), username, "SUCCESS", null, ipAddress));
        currentLoggedInUser = user;
        return user;
    }

    public void logout(User user, String ipAddress) {
        if (user != null) {
            AuditService.getInstance().logAction(user.getUserId(), "USER_LOGOUT", "users", user.getUserId(), "User logged out");
        }
        currentLoggedInUser = null;
    }

    public void changePassword(int userId, String oldPassword, String newPassword) throws InvalidLoginException {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new InvalidLoginException("User not found.");
        }
        if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash(), user.getSalt())) {
            throw new InvalidLoginException("Current password does not match.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new InvalidLoginException("New password must be at least 6 characters long.");
        }

        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hashPassword(newPassword, newSalt);
        userDAO.updatePassword(userId, newHash, newSalt);
        AuditService.getInstance().logAction(userId, "PASSWORD_CHANGED", "users", userId, "User updated their password");
    }
}
