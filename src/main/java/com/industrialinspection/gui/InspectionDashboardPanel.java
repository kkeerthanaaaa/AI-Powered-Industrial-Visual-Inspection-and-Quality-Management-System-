package com.industrialinspection.gui;

import com.industrialinspection.model.*;
import com.industrialinspection.model.enums.DefectLocation;
import com.industrialinspection.model.enums.InspectionDecision;
import com.industrialinspection.model.enums.SeverityLevel;
import com.industrialinspection.service.*;
import com.industrialinspection.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * 7. Visual Inspection Dashboard & Workflow Execution Panel.
 */
public class InspectionDashboardPanel extends JPanel {

    private final InspectionService inspectionService = new InspectionService();
    private final ProductService productService = new ProductService();
    private final BatchService batchService = new BatchService();
    private final ImageService imageService = new ImageService();
    private final AIPredictionService aiService = new AIPredictionService();
    private final DefectService defectService = new DefectService();
    private final User currentUser;

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;
    private final JComboBox<String> decisionFilter;

    public InspectionDashboardPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBackground(UITheme.BG_DARKEST);

        JLabel title = new JLabel("Visual Inspection Dashboard & Review Station");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBackground(UITheme.BG_DARKEST);

        searchField = UITheme.createTextField();
        searchField.setPreferredSize(new Dimension(170, 32));

        decisionFilter = UITheme.createComboBox(new String[]{"ALL", "PASS", "FAIL", "REQUIRES_REVIEW"});
        decisionFilter.setPreferredSize(new Dimension(150, 32));

        JButton filterBtn = UITheme.createSecondaryButton("Filter");
        filterBtn.addActionListener(e -> refreshTable());

        JButton newInspBtn = UITheme.createPrimaryButton("+ Start New Inspection Workflow");
        newInspBtn.addActionListener(e -> startInspectionWizard());
        if (!currentUser.canPerformInspection()) newInspBtn.setEnabled(false);

        JButton expBtn = UITheme.createSecondaryButton("Export CSV");
        expBtn.addActionListener(e -> exportCSV());

        controls.add(new JLabel("Search:") {{ setForeground(UITheme.TEXT_MUTED); }});
        controls.add(searchField);
        controls.add(decisionFilter);
        controls.add(filterBtn);
        controls.add(newInspBtn);
        controls.add(expBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Product Name", "Batch #", "Inspector", "AI Prediction", "Confidence", "Decision", "Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottomBar.setBackground(UITheme.BG_DARKEST);

        JButton viewAIBtn = UITheme.createSecondaryButton("View AI Inference & Bounding Boxes");
        viewAIBtn.addActionListener(e -> viewSelectedAIPrediction());

        JButton overrideBtn = UITheme.createSecondaryButton("Auditor Decision Override");
        overrideBtn.addActionListener(e -> overrideSelectedDecision());
        if (!currentUser.canApproveInspection()) overrideBtn.setEnabled(false);

        bottomBar.add(viewAIBtn);
        bottomBar.add(overrideBtn);
        add(bottomBar, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        String kw = searchField.getText().trim();
        String dec = (String) decisionFilter.getSelectedItem();
        List<InspectionRecord> list = inspectionService.searchInspections(kw, dec);

        for (InspectionRecord r : list) {
            tableModel.addRow(new Object[]{
                r.getInspectionId(),
                r.getProductName() != null ? r.getProductName() : ("Product #" + r.getProductId()),
                r.getBatchNumber() != null ? r.getBatchNumber() : ("Batch #" + r.getBatchId()),
                r.getInspectorName() != null ? r.getInspectorName() : ("User #" + r.getInspectorId()),
                r.getPredictedLabel() != null ? r.getPredictedLabel() : "PENDING",
                String.format("%.1f%%", r.getConfidenceScore()),
                r.getFinalDecision().name(),
                DateTimeUtil.formatTimestamp(r.getInspectionDate()),
                r.getStatus()
            });
        }
    }

    private void viewSelectedAIPrediction() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an inspection record from the table first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        InspectionRecord r = inspectionService.getInspectionById(id);
        if (r != null) {
            ProductImage img = imageService.getImageById(r.getImageId());
            AIPrediction pred = (r.getPredictionId() != null) ? aiService.getPredictionById(r.getPredictionId()) : null;
            AIPredictionViewerDialog dlg = new AIPredictionViewerDialog(SwingUtilities.getWindowAncestor(this), img, pred);
            dlg.setVisible(true);
        }
    }

    private void overrideSelectedDecision() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an inspection record to audit.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        InspectionRecord r = inspectionService.getInspectionById(id);
        if (r != null) {
            InspectionDecision newDec = (InspectionDecision) JOptionPane.showInputDialog(
                    this, "Quality Engineer Audit - Assign Final Decision:", "Audit Decision",
                    JOptionPane.QUESTION_MESSAGE, null, InspectionDecision.values(), r.getFinalDecision());

            if (newDec != null) {
                String remarks = JOptionPane.showInputDialog(this, "Auditor Review Remarks:", r.getRemarks());
                inspectionService.updateInspectionDecision(id, newDec, remarks, currentUser.getUserId());
                refreshTable();
            }
        }
    }

    private void startInspectionWizard() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Visual Inspection Wizard", true);
        dlg.setSize(520, 600);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel();
        pnl.setBackground(UITheme.BG_CARD);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Visual Inspection Workflow");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);
        pnl.add(title);
        pnl.add(Box.createRigidArea(new Dimension(0, 14)));

        List<ProductionBatch> batches = batchService.getAllBatches();
        JComboBox<ProductionBatch> bCb = new JComboBox<>(batches.toArray(new ProductionBatch[0]));
        pnl.add(createLabel("1. Select Production Batch:"));
        pnl.add(bCb);
        pnl.add(Box.createRigidArea(new Dimension(0, 10)));

        List<Product> products = productService.getAllProducts();
        JComboBox<Product> pCb = new JComboBox<>(products.toArray(new Product[0]));
        pnl.add(createLabel("2. Select Product Component:"));
        pnl.add(pCb);
        pnl.add(Box.createRigidArea(new Dimension(0, 10)));

        List<ProductImage> images = imageService.getAllImages();
        JComboBox<ProductImage> imgCb = new JComboBox<>(images.toArray(new ProductImage[0]));
        pnl.add(createLabel("3. Select Optical Capture Image:"));
        pnl.add(imgCb);
        pnl.add(Box.createRigidArea(new Dimension(0, 14)));

        JLabel aiStatusLbl = new JLabel("Click 'Run Simulated AI Inference' below");
        aiStatusLbl.setFont(UITheme.FONT_BOLD);
        aiStatusLbl.setForeground(UITheme.ACCENT_CYAN);
        pnl.add(aiStatusLbl);
        pnl.add(Box.createRigidArea(new Dimension(0, 8)));

        final AIPrediction[] predHolder = new AIPrediction[1];

        JButton runAIBtn = UITheme.createPrimaryButton("⚡ Run Simulated AI Prediction (Multithreaded)");
        runAIBtn.addActionListener(e -> {
            ProductImage selImg = (ProductImage) imgCb.getSelectedItem();
            if (selImg == null) {
                JOptionPane.showMessageDialog(dlg, "Please select an image.");
                return;
            }
            aiStatusLbl.setText("Running Edge Computer Vision Inference...");
            runAIBtn.setEnabled(false);

            aiService.runPredictionAsync(selImg, currentUser.getUserId());
            SwingWorker<AIPrediction, Void> w = new SwingWorker<>() {
                @Override
                protected AIPrediction doInBackground() throws Exception {
                    return aiService.runPrediction(selImg, currentUser.getUserId());
                }
                @Override
                protected void done() {
                    try {
                        predHolder[0] = get();
                        aiStatusLbl.setText("AI Flagged: " + predHolder[0].getPredictedLabel() + 
                                            " (" + String.format("%.1f%%", predHolder[0].getConfidenceScore()) + ")");
                    } catch (Exception ex) {
                        aiStatusLbl.setText("Inference Error: " + ex.getMessage());
                    }
                    runAIBtn.setEnabled(true);
                }
            };
            w.execute();
        });
        pnl.add(runAIBtn);
        pnl.add(Box.createRigidArea(new Dimension(0, 14)));

        JComboBox<InspectionDecision> decCb = new JComboBox<>(InspectionDecision.values());
        pnl.add(createLabel("4. Inspector Final Decision:"));
        pnl.add(decCb);
        pnl.add(Box.createRigidArea(new Dimension(0, 10)));

        JTextField remarksF = UITheme.createTextField();
        remarksF.setText("Optical visual inspection verified against GD&T drawing.");
        pnl.add(createLabel("5. Remarks:"));
        pnl.add(remarksF);
        pnl.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setBackground(UITheme.BG_CARD);

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        JButton saveBtn = UITheme.createSuccessButton("Save & Log Inspection");
        saveBtn.addActionListener(e -> {
            try {
                ProductionBatch b = (ProductionBatch) bCb.getSelectedItem();
                Product p = (Product) pCb.getSelectedItem();
                ProductImage img = (ProductImage) imgCb.getSelectedItem();
                if (b == null || p == null || img == null) {
                    JOptionPane.showMessageDialog(dlg, "Batch, Product, and Image are required.");
                    return;
                }

                Integer pId = (predHolder[0] != null) ? predHolder[0].getPredictionId() : null;
                InspectionDecision dec = (InspectionDecision) decCb.getSelectedItem();

                InspectionRecord rec = inspectionService.completeInspection(
                        p.getProductId(), b.getBatchId(), img.getImageId(),
                        currentUser.getUserId(), pId, dec,
                        remarksF.getText().trim(), "SUBMITTED"
                );

                // If FAIL, optionally register defect result
                if (dec == InspectionDecision.FAIL && predHolder[0] != null && !predHolder[0].isDefectFree()) {
                    defectService.recordDefectResult(
                            rec.getInspectionId(), 2, predHolder[0].getSeverity(),
                            predHolder[0].getDefectLocation(),
                            predHolder[0].getBoundingBoxX(), predHolder[0].getBoundingBoxY(),
                            predHolder[0].getBoundingBoxW(), predHolder[0].getBoundingBoxH(),
                            "Automated AI defect capture", currentUser.getUserId());
                }

                dlg.dispose();
                refreshTable();
                JOptionPane.showMessageDialog(this, "Inspection #" + rec.getInspectionId() + " successfully recorded!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error saving inspection: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancelBtn);
        btnBar.add(saveBtn);
        pnl.add(btnBar);

        dlg.setContentPane(pnl);
        dlg.setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MAIN);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("inspections_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                inspectionService.exportInspectionsToCSV(fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Exported successfully to " + fc.getSelectedFile().getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage());
            }
        }
    }
}
