package com.industrialinspection.gui;

import com.industrialinspection.model.DefectResult;
import com.industrialinspection.model.DefectType;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.SeverityLevel;
import com.industrialinspection.service.DefectService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * 9. Defect Taxonomy Classification & Log Panel.
 */
public class DefectClassificationPanel extends JPanel {

    private final DefectService defectService = new DefectService();
    private final User currentUser;
    private final JTable typeTable;
    private final DefaultTableModel typeModel;
    private final JTable resultsTable;
    private final DefaultTableModel resultsModel;

    public DefectClassificationPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARKEST);

        JLabel title = new JLabel("Defect Taxonomy & Classification Center");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(UITheme.BG_DARKEST);

        JButton addTypeBtn = UITheme.createPrimaryButton("+ Add Defect Type");
        addTypeBtn.addActionListener(e -> showAddDefectTypeDialog());
        if (!currentUser.canApproveInspection()) addTypeBtn.setEnabled(false);

        JButton exportBtn = UITheme.createSecondaryButton("Export Defects CSV");
        exportBtn.addActionListener(e -> exportCSV());

        btnPanel.add(addTypeBtn);
        btnPanel.add(exportBtn);

        header.add(title, BorderLayout.WEST);
        header.add(btnPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center: Tabbed pane separating Master Taxonomy and Confirmed Defect Results
        JTabbedPane tabbedPane = new JTabbedPane();
        UITheme.styleTabbedPane(tabbedPane);

        // Tab 1: Master Taxonomy
        String[] typeCols = {"ID", "Defect Code", "Defect Name", "Category", "Default Severity", "Remedy Description"};
        typeModel = new DefaultTableModel(typeCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        typeTable = new JTable(typeModel);
        UITheme.styleTable(typeTable);
        JScrollPane sp1 = new JScrollPane(typeTable);
        sp1.getViewport().setBackground(UITheme.BG_CARD);
        tabbedPane.addTab("Master Defect Catalog", sp1);

        // Tab 2: Logged Defect Incidents
        String[] resCols = {"Result ID", "Insp #", "Defect Code", "Defect Name", "Severity", "Location", "Comments"};
        resultsModel = new DefaultTableModel(resCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        resultsTable = new JTable(resultsModel);
        UITheme.styleTable(resultsTable);
        JScrollPane sp2 = new JScrollPane(resultsTable);
        sp2.getViewport().setBackground(UITheme.BG_CARD);
        tabbedPane.addTab("Logged Defect Incidents", sp2);

        add(tabbedPane, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        // Refresh master catalog
        typeModel.setRowCount(0);
        List<DefectType> types = defectService.getAllDefectTypes();
        for (DefectType t : types) {
            typeModel.addRow(new Object[]{
                t.getDefectTypeId(),
                t.getDefectCode(),
                t.getDefectName(),
                t.getCategory(),
                t.getDefaultSeverity().name(),
                t.getStandardRemedy()
            });
        }

        // Refresh logged results
        resultsModel.setRowCount(0);
        List<DefectResult> results = defectService.getAllDefectResults();
        for (DefectResult r : results) {
            resultsModel.addRow(new Object[]{
                r.getDefectResultId(),
                r.getInspectionId(),
                r.getDefectCode() != null ? r.getDefectCode() : "N/A",
                r.getDefectName() != null ? r.getDefectName() : ("Type #" + r.getDefectTypeId()),
                r.getSeverity().name(),
                r.getLocation().name(),
                r.getComments()
            });
        }
    }

    private void showAddDefectTypeDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Master Defect Type", true);
        dlg.setSize(420, 420);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new GridLayout(6, 2, 8, 10));
        pnl.setBackground(UITheme.BG_CARD);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField codeF = UITheme.createTextField();
        JTextField nameF = UITheme.createTextField();
        JTextField catF = UITheme.createTextField();
        JComboBox<SeverityLevel> sevCb = new JComboBox<>(SeverityLevel.values());
        JTextField remedyF = UITheme.createTextField();

        codeF.setText("DEF-NEW-01");
        catF.setText("Surface Flaw");

        pnl.add(createLabel("Defect Code:")); pnl.add(codeF);
        pnl.add(createLabel("Defect Name:")); pnl.add(nameF);
        pnl.add(createLabel("Category:")); pnl.add(catF);
        pnl.add(createLabel("Default Severity:")); pnl.add(sevCb);
        pnl.add(createLabel("Standard Remedy:")); pnl.add(remedyF);

        JButton saveBtn = UITheme.createPrimaryButton("Save Type");
        saveBtn.addActionListener(e -> {
            try {
                DefectType dt = new DefectType();
                dt.setDefectCode(codeF.getText().trim());
                dt.setDefectName(nameF.getText().trim());
                dt.setCategory(catF.getText().trim());
                dt.setDefaultSeverity((SeverityLevel) sevCb.getSelectedItem());
                dt.setStandardRemedy(remedyF.getText().trim());
                dt.setDescription(nameF.getText().trim());

                defectService.addDefectType(dt, currentUser.getUserId());
                dlg.dispose();
                refreshData();
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

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("defects_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                defectService.exportDefectsToCSV(fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Defects exported successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage());
            }
        }
    }
}
