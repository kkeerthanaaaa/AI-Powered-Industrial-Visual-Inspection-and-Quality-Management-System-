package com.industrialinspection.gui;

import com.industrialinspection.model.CorrectiveAction;
import com.industrialinspection.model.InspectionRecord;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.ActionStatus;
import com.industrialinspection.service.CorrectiveActionService;
import com.industrialinspection.service.InspectionService;
import com.industrialinspection.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.util.List;

/**
 * 10. Corrective and Preventive Action (CAPA) Panel.
 */
public class CorrectiveActionPanel extends JPanel {

    private final CorrectiveActionService capaService = new CorrectiveActionService();
    private final InspectionService inspectionService = new InspectionService();
    private final User currentUser;

    private final JTable table;
    private final DefaultTableModel tableModel;

    public CorrectiveActionPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_DARKEST);

        JLabel title = new JLabel("CAPA - Corrective & Preventive Action Register");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBackground(UITheme.BG_DARKEST);

        JButton addBtn = UITheme.createPrimaryButton("+ Create CAPA Incident");
        addBtn.addActionListener(e -> showCreateDialog());
        if (!currentUser.canCreateCorrectiveAction()) addBtn.setEnabled(false);

        JButton expBtn = UITheme.createSecondaryButton("Export CSV");
        expBtn.addActionListener(e -> exportCSV());

        controls.add(addBtn);
        controls.add(expBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"Action ID", "Insp #", "Root Cause", "Action Taken", "Responsible Person", "Target Date", "Completed Date", "Status"};
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

        JButton updateStatusBtn = UITheme.createSecondaryButton("Update Action Status");
        updateStatusBtn.addActionListener(e -> updateStatus());
        if (!currentUser.canCreateCorrectiveAction()) updateStatusBtn.setEnabled(false);

        bottomBar.add(updateStatusBtn);
        add(bottomBar, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<CorrectiveAction> list = capaService.getAllActions();
        for (CorrectiveAction ca : list) {
            tableModel.addRow(new Object[]{
                ca.getActionId(),
                ca.getInspectionId(),
                ca.getRootCause(),
                ca.getCorrectiveAction(),
                ca.getResponsiblePerson(),
                DateTimeUtil.formatDate(ca.getTargetDate()),
                ca.getCompletionDate() != null ? DateTimeUtil.formatDate(ca.getCompletionDate()) : "PENDING",
                ca.getStatus().name()
            });
        }
    }

    private void showCreateDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Issue Corrective Action", true);
        dlg.setSize(480, 480);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new GridLayout(6, 2, 8, 10));
        pnl.setBackground(UITheme.BG_CARD);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        List<InspectionRecord> inspections = inspectionService.getAllInspections();
        JComboBox<InspectionRecord> inspCb = new JComboBox<>(inspections.toArray(new InspectionRecord[0]));

        JTextField rootF = UITheme.createTextField();
        JTextField actF = UITheme.createTextField();
        JTextField respF = UITheme.createTextField();
        respF.setText(currentUser.getFullName());
        JComboBox<ActionStatus> stCb = new JComboBox<>(ActionStatus.values());

        rootF.setText("Tooling wear and thermal spindle vibration");
        actF.setText("Replace cutter insert and re-zero spindle offset");

        pnl.add(createLabel("Related Inspection:")); pnl.add(inspCb);
        pnl.add(createLabel("Root Cause Analysis:")); pnl.add(rootF);
        pnl.add(createLabel("Corrective Action:")); pnl.add(actF);
        pnl.add(createLabel("Responsible Person:")); pnl.add(respF);
        pnl.add(createLabel("Initial Status:")); pnl.add(stCb);

        JButton saveBtn = UITheme.createPrimaryButton("Issue Action");
        saveBtn.addActionListener(e -> {
            try {
                InspectionRecord sel = (InspectionRecord) inspCb.getSelectedItem();
                int inspId = (sel != null) ? sel.getInspectionId() : 1;

                CorrectiveAction ca = new CorrectiveAction();
                ca.setInspectionId(inspId);
                ca.setRootCause(rootF.getText().trim());
                ca.setCorrectiveAction(actF.getText().trim());
                ca.setResponsiblePerson(respF.getText().trim());
                ca.setStatus((ActionStatus) stCb.getSelectedItem());
                ca.setTargetDate(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000L));

                capaService.createCorrectiveAction(ca, currentUser.getUserId());
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

    private void updateStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a CAPA record from table.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        ActionStatus st = (ActionStatus) JOptionPane.showInputDialog(
                this, "Select updated CAPA status:", "Update Status",
                JOptionPane.QUESTION_MESSAGE, null, ActionStatus.values(), ActionStatus.COMPLETED);

        if (st != null) {
            String remarks = JOptionPane.showInputDialog(this, "Verification remarks:");
            capaService.updateStatus(id, st, remarks, currentUser.getUserId());
            refreshTable();
        }
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("capa_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                capaService.exportActionsToCSV(fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "CAPA register exported successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage());
            }
        }
    }
}
