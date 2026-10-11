package com.industrialinspection.gui;

import com.industrialinspection.gui.components.BarChartPanel;
import com.industrialinspection.gui.components.LineChartPanel;
import com.industrialinspection.gui.components.PieChartPanel;
import com.industrialinspection.model.User;
import com.industrialinspection.service.DashboardService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/**
 * 11. Real-Time Industrial Quality Analytics Dashboard.
 * Integrates custom Java2D Graphics2D charting components.
 */
public class QualityDashboardPanel extends JPanel {

    private final DashboardService dashboardService = new DashboardService();
    private final User currentUser;

    private final JPanel kpiGrid;
    private final BarChartPanel barChart;
    private final PieChartPanel pieChart;
    private final LineChartPanel lineChart;

    public QualityDashboardPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARKEST);

        JLabel title = new JLabel("Quality Analytics & Statistical Process Control (SPC)");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JButton refreshBtn = UITheme.createPrimaryButton("🔄 Live Refresh");
        refreshBtn.addActionListener(e -> reloadData());

        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center Scrollable View
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.BG_DARKEST);

        // Top KPI Cards Grid
        kpiGrid = new JPanel(new GridLayout(1, 6, 12, 12));
        kpiGrid.setBackground(UITheme.BG_DARKEST);
        kpiGrid.setMaximumSize(new Dimension(2000, 95));
        content.add(kpiGrid);
        content.add(Box.createRigidArea(new Dimension(0, 14)));

        // Charts Grid (Bar + Pie)
        JPanel topCharts = new JPanel(new GridLayout(1, 2, 14, 14));
        topCharts.setBackground(UITheme.BG_DARKEST);
        topCharts.setMaximumSize(new Dimension(2000, 260));

        barChart = new BarChartPanel("Machine-wise Rejection Volume", dashboardService.getMachineWiseDefects());
        pieChart = new PieChartPanel("Optical Defect Taxonomy Distribution", dashboardService.getDefectDistribution());

        topCharts.add(barChart);
        topCharts.add(pieChart);
        content.add(topCharts);
        content.add(Box.createRigidArea(new Dimension(0, 14)));

        // Bottom Trend Chart (Line)
        JPanel bottomChart = new JPanel(new BorderLayout());
        bottomChart.setBackground(UITheme.BG_DARKEST);
        bottomChart.setMaximumSize(new Dimension(2000, 230));

        lineChart = new LineChartPanel("First-Pass Quality Yield % (6-Month Trend)", dashboardService.getMonthlyQualityTrend());
        bottomChart.add(lineChart, BorderLayout.CENTER);
        content.add(bottomChart);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BG_DARKEST);
        add(scroll, BorderLayout.CENTER);

        reloadData();
    }

    public void reloadData() {
        Map<String, Object> kpi = dashboardService.getOverallSummaryKPIs();

        kpiGrid.removeAll();
        kpiGrid.add(createKpiCard("TOTAL PRODUCTS", String.valueOf(kpi.get("totalProducts")), UITheme.ACCENT_BLUE));
        kpiGrid.add(createKpiCard("INSPECTIONS", String.valueOf(kpi.get("totalInspections")), UITheme.TEXT_MAIN));
        kpiGrid.add(createKpiCard("PASSED YIELD", String.valueOf(kpi.get("passedInspections")), UITheme.ACCENT_GREEN));
        kpiGrid.add(createKpiCard("FAILED / REJECT", String.valueOf(kpi.get("failedInspections")), UITheme.ACCENT_RED));
        kpiGrid.add(createKpiCard("DEFECT RATE", kpi.get("defectPercentage") + "%", UITheme.ACCENT_AMBER));
        kpiGrid.add(createKpiCard("CRITICAL DEFECTS", String.valueOf(kpi.get("criticalDefects")), UITheme.ACCENT_RED));
        kpiGrid.revalidate();
        kpiGrid.repaint();

        barChart.setData(dashboardService.getMachineWiseDefects());
        pieChart.setData(dashboardService.getDefectDistribution());
        lineChart.setData(dashboardService.getMonthlyQualityTrend());
    }

    private JPanel createKpiCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(UITheme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(accent);

        card.add(l, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }
}
