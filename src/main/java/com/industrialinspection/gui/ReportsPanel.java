package com.industrialinspection.gui;

import com.industrialinspection.model.QualityReport;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.ReportType;
import com.industrialinspection.service.QualityReportService;
import com.industrialinspection.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * 14. Industrial Quality Reporting & Export Station.
 * Produces all 7 standardized manufacturing reports in CSV and TXT.
 */
public class ReportsPanel extends JPanel {

    private final QualityReportService reportService = new QualityReportService();
    private final User currentUser;

    private final JComboBox<ReportType> reportTypeCb;
    private final JComboBox<String> formatCb;
    private final JTable historyTable;
    private final DefaultTableModel historyModel;
    private final JTextArea previewArea;

    public ReportsPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Generator Bar
        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        JLabel title = new JLabel("Generate Report:");
        title.setFont(UITheme.FONT_HEADER_MED);
        title.setForeground(UITheme.TEXT_MAIN);

        reportTypeCb = new JComboBox<>(ReportType.values());
        reportTypeCb.setPreferredSize(new Dimension(250, 32));

        formatCb = new JComboBox<>(new String[]{"CSV", "TXT"});
        formatCb.setPreferredSize(new Dimension(80, 32));

        JButton genBtn = UITheme.createPrimaryButton("⚡ Generate Report File");
        genBtn.addActionListener(e -> generateReport());
        if (!currentUser.canGenerateReports()) genBtn.setEnabled(false);

        topBar.add(title);
        topBar.add(reportTypeCb);
        topBar.add(new JLabel("Format:") {{ setForeground(UITheme.TEXT_MUTED); }});
        topBar.add(formatCb);
        topBar.add(genBtn);
        add(topBar, BorderLayout.NORTH);

        // Center Split: History on Left, Preview on Right
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBackground(UITheme.BG_DARKEST);
        split.setDividerLocation(460);

        // History Table
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBackground(UITheme.BG_DARKEST);

        JLabel hLbl = new JLabel("Generated Quality Reports Archive");
        hLbl.setFont(UITheme.FONT_BOLD);
        hLbl.setForeground(UITheme.TEXT_MAIN);
        left.add(hLbl, BorderLayout.NORTH);

        String[] cols = {"ID", "Report Title", "Format", "Generated At"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        UITheme.styleTable(historyTable);
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) previewSelectedReport();
        });

        JScrollPane sp = new JScrollPane(historyTable);
        sp.getViewport().setBackground(UITheme.BG_CARD);
        left.add(sp, BorderLayout.CENTER);
        split.setLeftComponent(left);

        // Preview Area on Right
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBackground(UITheme.BG_DARKEST);

        JLabel pLbl = new JLabel("Report Content Preview");
        pLbl.setFont(UITheme.FONT_BOLD);
        pLbl.setForeground(UITheme.ACCENT_CYAN);
        right.add(pLbl, BorderLayout.NORTH);

        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(UITheme.FONT_MONO);
        previewArea.setBackground(UITheme.BG_INPUT);
        previewArea.setForeground(UITheme.TEXT_MAIN);
        previewArea.setText("Select a report from the table to inspect its content...");

        JScrollPane textSp = new JScrollPane(previewArea);
        textSp.getViewport().setBackground(UITheme.BG_INPUT);
        right.add(textSp, BorderLayout.CENTER);
        split.setRightComponent(right);

        add(split, BorderLayout.CENTER);

        refreshHistory();
    }

    public void refreshHistory() {
        historyModel.setRowCount(0);
        List<QualityReport> list = reportService.getAllGeneratedReports();
        for (QualityReport qr : list) {
            historyModel.addRow(new Object[]{
                qr.getReportId(),
                qr.getTitle(),
                qr.getFileFormat(),
                DateTimeUtil.formatTimestamp(qr.getGeneratedAt())
            });
        }
    }

    private void generateReport() {
        ReportType type = (ReportType) reportTypeCb.getSelectedItem();
        String format = (String) formatCb.getSelectedItem();
        try {
            QualityReport rep = reportService.generateReport(type, format, currentUser.getUserId(), null);
            refreshHistory();
            loadFilePreview(new File(rep.getFilePath()));
            JOptionPane.showMessageDialog(this,
                    "Report generated successfully!\nFile: " + rep.getFilePath(),
                    "Report Ready", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Generation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void previewSelectedReport() {
        int row = historyTable.getSelectedRow();
        if (row < 0) return;
        int id = (int) historyModel.getValueAt(row, 0);
        QualityReport r = reportService.getReportById(id);
        if (r != null && r.getFilePath() != null) {
            loadFilePreview(new File(r.getFilePath()));
        }
    }

    private void loadFilePreview(File f) {
        if (!f.exists()) {
            previewArea.setText("[File not found on disk: " + f.getAbsolutePath() + "]");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            StringBuilder sb = new StringBuilder();
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < 200) {
                sb.append(line).append("\n");
                count++;
            }
            previewArea.setText(sb.toString());
            previewArea.setCaretPosition(0);
        } catch (Exception ex) {
            previewArea.setText("Error reading file: " + ex.getMessage());
        }
    }
}
