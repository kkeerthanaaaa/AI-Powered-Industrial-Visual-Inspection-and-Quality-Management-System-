package com.industrialinspection.dao;

import com.industrialinspection.model.User;
import java.util.List;

public interface UserDAO {
    void addUser(User user);
    User getUserById(int userId);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    void updateUser(User user);
    void updatePassword(int userId, String passwordHash, String salt);
    void updateStatus(int userId, String status);
    void recordFailedAttempt(int userId);
    void resetFailedAttempts(int userId);
    void deleteUser(int userId);
}
