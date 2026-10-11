package com.industrialinspection.gui;

import com.industrialinspection.model.Operator;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.OperatorStatus;
import com.industrialinspection.model.enums.Shift;
import com.industrialinspection.service.OperatorService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 13. Operator & Quality Floor Inspector Roster Panel.
 */
public class OperatorManagementPanel extends JPanel {

    private final OperatorService operatorService = new OperatorService();
    private final User currentUser;

    private final JTable table;
    private final DefaultTableModel tableModel;

    public OperatorManagementPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_DARKEST);

        JLabel title = new JLabel("Certified Inspection Operators & Floor Shift Roster");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBackground(UITheme.BG_DARKEST);

        JButton addBtn = UITheme.createPrimaryButton("+ Register Operator");
        addBtn.addActionListener(e -> showAddEditDialog(null));
        if (!currentUser.canManageOperators()) addBtn.setEnabled(false);

        controls.add(addBtn);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Employee Code", "Department", "Shift", "Contact", "Status"};
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

        JButton editBtn = UITheme.createSecondaryButton("Edit Operator");
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                Operator o = operatorService.getOperatorById(id);
                showAddEditDialog(o);
            } else {
                JOptionPane.showMessageDialog(this, "Select an operator from table.");
            }
        });

        JButton statusBtn = UITheme.createSecondaryButton("Update Shift / Status");
        statusBtn.addActionListener(e -> updateStatus());

        if (!currentUser.canManageOperators()) {
            editBtn.setEnabled(false);
            statusBtn.setEnabled(false);
        }

        bottomBar.add(editBtn);
        bottomBar.add(statusBtn);
        add(bottomBar, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Operator> list = operatorService.getAllOperators();
        for (Operator o : list) {
            tableModel.addRow(new Object[]{
                o.getOperatorId(),
                o.getName(),
                o.getEmployeeCode(),
                o.getDepartment(),
                o.getShift().name(),
                o.getContact(),
                o.getStatus().name()
            });
        }
    }

    private void updateStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an operator.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        OperatorStatus st = (OperatorStatus) JOptionPane.showInputDialog(
                this, "Choose Operator Status:", "Operator Status",
                JOptionPane.QUESTION_MESSAGE, null, OperatorStatus.values(), OperatorStatus.ACTIVE);
        if (st != null) {
            operatorService.updateStatus(id, st, currentUser.getUserId());
            refreshTable();
        }
    }

    private void showAddEditDialog(Operator existing) {
        boolean isEdit = (existing != null);
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Operator" : "Register Operator", true);
        dlg.setSize(420, 440);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new GridLayout(7, 2, 8, 10));
        pnl.setBackground(UITheme.BG_CARD);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameF = UITheme.createTextField();
        JTextField codeF = UITheme.createTextField();
        JTextField deptF = UITheme.createTextField();
        JTextField phoneF = UITheme.createTextField();
        JComboBox<Shift> shiftCb = new JComboBox<>(Shift.values());
        JComboBox<OperatorStatus> statCb = new JComboBox<>(OperatorStatus.values());

        if (isEdit) {
            nameF.setText(existing.getName());
            codeF.setText(existing.getEmployeeCode());
            deptF.setText(existing.getDepartment());
            phoneF.setText(existing.getContact());
            shiftCb.setSelectedItem(existing.getShift());
            statCb.setSelectedItem(existing.getStatus());
        } else {
            codeF.setText("EMP-OP-" + (tableModel.getRowCount() + 105));
            deptF.setText("Optical Metrology");
            phoneF.setText("+91 98765 00000");
        }

        pnl.add(createLabel("Operator Name:")); pnl.add(nameF);
        pnl.add(createLabel("Employee Code:")); pnl.add(codeF);
        pnl.add(createLabel("Department:")); pnl.add(deptF);
        pnl.add(createLabel("Contact Phone:")); pnl.add(phoneF);
        pnl.add(createLabel("Assigned Shift:")); pnl.add(shiftCb);
        pnl.add(createLabel("Status:")); pnl.add(statCb);

        JButton saveBtn = UITheme.createPrimaryButton("Save Operator");
        saveBtn.addActionListener(e -> {
            try {
                Operator o = isEdit ? existing : new Operator();
                o.setName(nameF.getText().trim());
                o.setEmployeeCode(codeF.getText().trim());
                o.setDepartment(deptF.getText().trim());
                o.setContact(phoneF.getText().trim());
                o.setShift((Shift) shiftCb.getSelectedItem());
                o.setStatus((OperatorStatus) statCb.getSelectedItem());

                if (isEdit) {
                    operatorService.updateOperator(o, currentUser.getUserId());
                } else {
                    operatorService.addOperator(o, currentUser.getUserId());
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
