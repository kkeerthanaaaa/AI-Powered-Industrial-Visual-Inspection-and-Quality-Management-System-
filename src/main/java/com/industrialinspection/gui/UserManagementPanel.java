package com.industrialinspection.gui;

import com.industrialinspection.dao.UserDAO;
import com.industrialinspection.dao.UserDAOImpl;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.UserRole;
import com.industrialinspection.service.AuditService;
import com.industrialinspection.util.PasswordUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 15. User & Role Authorization Administration Panel.
 */
public class UserManagementPanel extends JPanel {

    private final UserDAO userDAO = new UserDAOImpl();
    private final User currentUser;
    private final JTable table;
    private final DefaultTableModel tableModel;

    public UserManagementPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_DARKEST);

        JLabel title = new JLabel("Security & Role-Based Access Control (RBAC)");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBackground(UITheme.BG_DARKEST);

        JButton addBtn = UITheme.createPrimaryButton("+ Create New User");
        addBtn.addActionListener(e -> showCreateUserDialog());
        if (!currentUser.canManageUsers()) addBtn.setEnabled(false);

        controls.add(addBtn);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"User ID", "Username", "Full Name", "Email", "Assigned Role", "Status", "Failed Tries"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(UITheme.BG_CARD);
        add(sp, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottomBar.setBackground(UITheme.BG_DARKEST);

        JButton editRoleBtn = UITheme.createSecondaryButton("Change Role / Status");
        editRoleBtn.addActionListener(e -> updateRoleOrStatus());

        JButton resetPassBtn = UITheme.createSecondaryButton("Reset Password");
        resetPassBtn.addActionListener(e -> resetUserPassword());

        JButton delBtn = UITheme.createDangerButton("Deactivate / Delete");
        delBtn.addActionListener(e -> deleteSelectedUser());

        if (!currentUser.canManageUsers()) {
            editRoleBtn.setEnabled(false);
            resetPassBtn.setEnabled(false);
            delBtn.setEnabled(false);
        }

        bottomBar.add(editRoleBtn);
        bottomBar.add(resetPassBtn);
        bottomBar.add(delBtn);
        add(bottomBar, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            tableModel.addRow(new Object[]{
                u.getUserId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getRole().name(),
                u.getStatus(),
                u.getFailedAttempts()
            });
        }
    }

    private void showCreateUserDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create New User Account", true);
        dlg.setSize(420, 440);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new GridLayout(6, 2, 8, 10));
        pnl.setBackground(UITheme.BG_CARD);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField userF = UITheme.createTextField();
        JTextField nameF = UITheme.createTextField();
        JTextField emailF = UITheme.createTextField();
        JPasswordField passF = UITheme.createPasswordField();
        JComboBox<UserRole> roleCb = new JComboBox<>(UserRole.values());

        passF.setText("Pass@123");

        pnl.add(createLabel("Username:")); pnl.add(userF);
        pnl.add(createLabel("Full Name:")); pnl.add(nameF);
        pnl.add(createLabel("Corporate Email:")); pnl.add(emailF);
        pnl.add(createLabel("Initial Password:")); pnl.add(passF);
        pnl.add(createLabel("Security Role:")); pnl.add(roleCb);

        JButton saveBtn = UITheme.createPrimaryButton("Create Account");
        saveBtn.addActionListener(e -> {
            try {
                String u = userF.getText().trim();
                String rawP = new String(passF.getPassword());
                if (u.isEmpty() || rawP.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Username and password required.");
                    return;
                }

                String salt = PasswordUtil.generateSalt();
                String hash = PasswordUtil.hashPassword(rawP, salt);

                User nu = new User();
                nu.setUsername(u);
                nu.setFullName(nameF.getText().trim());
                nu.setEmail(emailF.getText().trim());
                nu.setPasswordHash(hash);
                nu.setSalt(salt);
                nu.setRole((UserRole) roleCb.getSelectedItem());
                nu.setStatus("ACTIVE");

                userDAO.addUser(nu);
                AuditService.getInstance().logAction(currentUser.getUserId(), "USER_CREATED", "users", nu.getUserId(),
                        "Admin created new user: " + nu.getUsername() + " with role " + nu.getRole());

                dlg.dispose();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        pnl.add(cancelBtn);
        pnl.add(saveBtn);

        dlg.setContentPane(pnl);
        dlg.setVisible(true);
    }

    private void updateRoleOrStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user from table.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        User u = userDAO.getUserById(id);
        if (u != null) {
            UserRole newRole = (UserRole) JOptionPane.showInputDialog(this, "Assign New Role:", "Update Role",
                    JOptionPane.QUESTION_MESSAGE, null, UserRole.values(), u.getRole());
            if (newRole != null) {
                u.setRole(newRole);
                userDAO.updateUser(u);
                AuditService.getInstance().logAction(currentUser.getUserId(), "USER_ROLE_CHANGED", "users", id,
                        "Admin modified role of " + u.getUsername() + " to " + newRole);
                refreshTable();
            }
        }
    }

    private void resetUserPassword() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user to reset password.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String newPass = JOptionPane.showInputDialog(this, "Enter new temporary password (min 6 chars):", "Pass@123");
        if (newPass != null && newPass.length() >= 6) {
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(newPass, salt);
            userDAO.updatePassword(id, hash, salt);
            AuditService.getInstance().logAction(currentUser.getUserId(), "USER_PASSWORD_RESET", "users", id,
                    "Admin reset password for user #" + id);
            JOptionPane.showMessageDialog(this, "Password reset successfully!");
        }
    }

    private void deleteSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        if (id == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account.");
            return;
        }
        int opt = JOptionPane.showConfirmDialog(this, "Deactivate / delete user #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            userDAO.deleteUser(id);
            refreshTable();
        }
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }
}
