package com.industrialinspection.gui;

import com.industrialinspection.model.Machine;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.MachineStatus;
import com.industrialinspection.service.MachineService;
import com.industrialinspection.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

/**
 * 12. Industrial Machinery & Cell Management Panel.
 */
public class MachineManagementPanel extends JPanel {

    private final MachineService machineService = new MachineService();
    private final User currentUser;

    private final JTable table;
    private final DefaultTableModel tableModel;

    public MachineManagementPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_DARKEST);

        JLabel title = new JLabel("Manufacturing Machines & Optical Cell Station Registry");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBackground(UITheme.BG_DARKEST);

        JButton addBtn = UITheme.createPrimaryButton("+ Register Machine");
        addBtn.addActionListener(e -> showAddEditDialog(null));
        if (!currentUser.canManageMachines()) addBtn.setEnabled(false);

        controls.add(addBtn);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Code", "Machine Name", "Type", "Production Line", "Status", "Installed Date", "Last Maintenance"};
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

        JButton statusBtn = UITheme.createSecondaryButton("Toggle Machine Status");
        statusBtn.addActionListener(e -> toggleStatus());
        if (!currentUser.canManageMachines()) statusBtn.setEnabled(false);

        JButton editBtn = UITheme.createSecondaryButton("Edit Machine");
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                Machine m = machineService.getMachineById(id);
                showAddEditDialog(m);
            } else {
                JOptionPane.showMessageDialog(this, "Select a machine to edit.");
            }
        });
        if (!currentUser.canManageMachines()) editBtn.setEnabled(false);

        bottomBar.add(statusBtn);
        bottomBar.add(editBtn);
        add(bottomBar, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Machine> list = machineService.getAllMachines();
        for (Machine m : list) {
            tableModel.addRow(new Object[]{
                m.getMachineId(),
                m.getMachineCode(),
                m.getMachineName(),
                m.getMachineType(),
                m.getProductionLine(),
                m.getStatus().name(),
                DateTimeUtil.formatDate(m.getInstallationDate()),
                m.getLastMaintenance() != null ? DateTimeUtil.formatDate(m.getLastMaintenance()) : "N/A"
            });
        }
    }

    private void toggleStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a machine from the table.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        MachineStatus st = (MachineStatus) JOptionPane.showInputDialog(
                this, "Select Machine Status:", "Machine Status",
                JOptionPane.QUESTION_MESSAGE, null, MachineStatus.values(), MachineStatus.ACTIVE);
        if (st != null) {
            machineService.updateStatus(id, st, currentUser.getUserId());
            refreshTable();
        }
    }

    private void showAddEditDialog(Machine existing) {
        boolean isEdit = (existing != null);
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Machine" : "Register Machine", true);
        dlg.setSize(420, 420);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new GridLayout(6, 2, 8, 10));
        pnl.setBackground(UITheme.BG_CARD);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField codeF = UITheme.createTextField();
        JTextField nameF = UITheme.createTextField();
        JTextField typeF = UITheme.createTextField();
        JTextField lineF = UITheme.createTextField();
        JComboBox<MachineStatus> statCb = new JComboBox<>(MachineStatus.values());

        if (isEdit) {
            codeF.setText(existing.getMachineCode());
            nameF.setText(existing.getMachineName());
            typeF.setText(existing.getMachineType());
            lineF.setText(existing.getProductionLine());
            statCb.setSelectedItem(existing.getStatus());
        } else {
            codeF.setText("CELL-00" + (tableModel.getRowCount() + 1));
            typeF.setText("Optical Metrology Station");
            lineF.setText("Auto-Line-2");
        }

        pnl.add(createLabel("Machine Code:")); pnl.add(codeF);
        pnl.add(createLabel("Machine Name:")); pnl.add(nameF);
        pnl.add(createLabel("Machine Type:")); pnl.add(typeF);
        pnl.add(createLabel("Production Line:")); pnl.add(lineF);
        pnl.add(createLabel("Status:")); pnl.add(statCb);

        JButton saveBtn = UITheme.createPrimaryButton("Save");
        saveBtn.addActionListener(e -> {
            try {
                Machine m = isEdit ? existing : new Machine();
                m.setMachineCode(codeF.getText().trim());
                m.setMachineName(nameF.getText().trim());
                m.setMachineType(typeF.getText().trim());
                m.setProductionLine(lineF.getText().trim());
                m.setStatus((MachineStatus) statCb.getSelectedItem());
                if (!isEdit) {
                    m.setInstallationDate(new Date(System.currentTimeMillis()));
                    m.setLastMaintenance(new Date(System.currentTimeMillis()));
                    machineService.addMachine(m, currentUser.getUserId());
                } else {
                    machineService.updateMachine(m, currentUser.getUserId());
                }
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

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }
}
