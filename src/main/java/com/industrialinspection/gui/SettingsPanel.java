package com.industrialinspection.gui;

import com.industrialinspection.dao.SystemSettingsDAO;
import com.industrialinspection.dao.SystemSettingsDAOImpl;
import com.industrialinspection.model.SystemSettings;
import com.industrialinspection.model.User;
import com.industrialinspection.service.AuditService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 16. System Settings & Configuration Panel.
 */
public class SettingsPanel extends JPanel {

    private final SystemSettingsDAO settingsDAO = new SystemSettingsDAOImpl();
    private final User currentUser;
    private final JTable table;
    private final DefaultTableModel tableModel;

    public SettingsPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_DARKEST);

        JLabel title = new JLabel("System Tuning & Global Quality Parameters");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JLabel sub = new JLabel("Configure AI thresholds, camera mounts, file size restrictions, and compliance rules");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);

        topBar.add(title, BorderLayout.NORTH);
        topBar.add(sub, BorderLayout.SOUTH);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"Key", "Group", "Value", "Description", "Last Updated"};
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

        JButton editBtn = UITheme.createPrimaryButton("Modify Selected Setting Value");
        editBtn.addActionListener(e -> modifySetting());
        if (!currentUser.canManageUsers()) editBtn.setEnabled(false);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTable());

        bottomBar.add(editBtn);
        bottomBar.add(refreshBtn);
        add(bottomBar, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<SystemSettings> list = settingsDAO.getAllSettings();
        for (SystemSettings s : list) {
            tableModel.addRow(new Object[]{
                s.getSettingKey(),
                s.getSettingGroup(),
                s.getSettingValue(),
                s.getDescription(),
                s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : "Static"
            });
        }
    }

    private void modifySetting() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a setting to modify.");
            return;
        }
        String key = (String) tableModel.getValueAt(row, 0);
        String currentVal = (String) tableModel.getValueAt(row, 2);
        String group = (String) tableModel.getValueAt(row, 1);
        String desc = (String) tableModel.getValueAt(row, 3);

        String newVal = JOptionPane.showInputDialog(this, "Enter new value for '" + key + "':", currentVal);
        if (newVal != null && !newVal.trim().isEmpty()) {
            settingsDAO.setSetting(key, newVal.trim(), group, desc);
            AuditService.getInstance().logAction(currentUser.getUserId(), "SETTING_CHANGED", "system_settings", 0,
                    "Modified setting " + key + " = " + newVal.trim());
            refreshTable();
        }
    }
}
