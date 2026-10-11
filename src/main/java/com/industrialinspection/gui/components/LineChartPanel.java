package com.industrialinspection.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Pure Java2D Line Chart Component.
 * Plots quality yield curves and trends.
 */
public class LineChartPanel extends JPanel {

    private final Map<String, Double> data = new LinkedHashMap<>();
    private String title = "Monthly Pass Rate Yield (%)";

    public LineChartPanel() {
        setBackground(new Color(24, 28, 38));
        setPreferredSize(new Dimension(360, 240));
    }

    public LineChartPanel(String title, Map<String, Double> initialData) {
        this();
        this.title = title;
        if (initialData != null) this.data.putAll(initialData);
    }

    public void setData(Map<String, Double> newData) {
        data.clear();
        if (newData != null) data.putAll(newData);
        repaint();
    }

    public void setTitle(String title) {
        this.title = title;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Title
        g2.setColor(new Color(220, 230, 245));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.drawString(title, 20, 24);

        if (data.size() < 2) {
            g2.setColor(new Color(140, 150, 170));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.drawString("Insufficient data to plot trend line", w / 2 - 80, h / 2);
            g2.dispose();
            return;
        }

        int marginL = 50;
        int marginR = 25;
        int marginT = 45;
        int marginB = 40;

        int chartW = w - marginL - marginR;
        int chartH = h - marginT - marginB;

        double minVal = 90.0;
        double maxVal = 100.0;

        // Grid lines
        g2.setColor(new Color(45, 52, 70));
        for (int i = 0; i <= 4; i++) {
            int y = marginT + chartH - (chartH * i / 4);
            g2.drawLine(marginL, y, marginL + chartW, y);
            double val = minVal + ((maxVal - minVal) * i / 4.0);
            g2.setColor(new Color(130, 140, 160));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(String.format("%.0f%%", val), 15, y + 4);
            g2.setColor(new Color(45, 52, 70));
        }

        int n = data.size();
        int[] xs = new int[n];
        int[] ys = new int[n];

        int idx = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double v = Math.max(minVal, Math.min(maxVal, entry.getValue()));
            xs[idx] = marginL + (idx * chartW / (n - 1));
            ys[idx] = marginT + chartH - (int) (((v - minVal) / (maxVal - minVal)) * chartH);

            // Month text below
            g2.setColor(new Color(160, 175, 200));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            String key = entry.getKey();
            g2.drawString(key, xs[idx] - (fm.stringWidth(key) / 2), marginT + chartH + 18);

            idx++;
        }

        // Polygon area under line with soft gradient
        Polygon poly = new Polygon();
        poly.addPoint(xs[0], marginT + chartH);
        for (int i = 0; i < n; i++) {
            poly.addPoint(xs[i], ys[i]);
        }
        poly.addPoint(xs[n - 1], marginT + chartH);

        GradientPaint fillGp = new GradientPaint(0, marginT, new Color(0, 210, 140, 70), 0, marginT + chartH, new Color(0, 210, 140, 0));
        g2.setPaint(fillGp);
        g2.fill(poly);

        // Polyline
        g2.setColor(new Color(0, 230, 150));
        g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawPolyline(xs, ys, n);

        // Data point dots & badges
        idx = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            g2.setColor(new Color(24, 28, 38));
            g2.fillOval(xs[idx] - 5, ys[idx] - 5, 10, 10);
            g2.setColor(new Color(0, 230, 150));
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawOval(xs[idx] - 5, ys[idx] - 5, 10, 10);

            // Value label
            g2.setColor(new Color(240, 245, 255));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String valStr = String.format("%.1f%%", entry.getValue());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, xs[idx] - (fm.stringWidth(valStr) / 2), ys[idx] - 8);

            idx++;
        }

        g2.dispose();
    }
}
