package com.industrialinspection.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Pure Java2D Bar Chart Component.
 * Zero external libraries; rendered with Graphics2D.
 */
public class BarChartPanel extends JPanel {

    private final Map<String, Integer> data = new LinkedHashMap<>();
    private String title = "Machine Defect Distribution";

    public BarChartPanel() {
        setBackground(new Color(24, 28, 38));
        setPreferredSize(new Dimension(360, 240));
    }

    public BarChartPanel(String title, Map<String, Integer> initialData) {
        this();
        this.title = title;
        if (initialData != null) this.data.putAll(initialData);
    }

    public void setData(Map<String, Integer> newData) {
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

        // Chart Header Title
        g2.setColor(new Color(220, 230, 245));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.drawString(title, 20, 24);

        if (data.isEmpty()) {
            g2.setColor(new Color(140, 150, 170));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.drawString("No data available", w / 2 - 40, h / 2);
            g2.dispose();
            return;
        }

        int maxVal = data.values().stream().max(Integer::compareTo).orElse(1);
        if (maxVal == 0) maxVal = 1;

        int marginL = 50;
        int marginR = 25;
        int marginT = 45;
        int marginB = 40;

        int chartW = w - marginL - marginR;
        int chartH = h - marginT - marginB;

        // Grid lines
        g2.setColor(new Color(45, 52, 70));
        g2.drawLine(marginL, marginT + chartH, marginL + chartW, marginT + chartH);
        for (int i = 1; i <= 4; i++) {
            int y = marginT + chartH - (chartH * i / 4);
            g2.drawLine(marginL, y, marginL + chartW, y);
            int labelVal = (maxVal * i) / 4;
            g2.setColor(new Color(130, 140, 160));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(String.valueOf(labelVal), 15, y + 4);
            g2.setColor(new Color(45, 52, 70));
        }

        int barCount = data.size();
        int slotW = chartW / Math.max(1, barCount);
        int barW = Math.max(16, (int) (slotW * 0.55));

        int idx = 0;
        Color[] barColors = {
            new Color(0, 168, 255),
            new Color(0, 210, 140),
            new Color(255, 170, 0),
            new Color(255, 75, 75),
            new Color(156, 39, 176)
        };

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int val = entry.getValue();
            int bH = (int) (((double) val / maxVal) * chartH);
            int x = marginL + (idx * slotW) + (slotW - barW) / 2;
            int y = marginT + chartH - bH;

            Color c = barColors[idx % barColors.length];
            GradientPaint gp = new GradientPaint(x, y, c, x + barW, y + bH, c.darker());
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, barW, bH, 6, 6);

            // Value badge above bar
            g2.setColor(new Color(240, 245, 255));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String valStr = String.valueOf(val);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, x + (barW - fm.stringWidth(valStr)) / 2, y - 4);

            // Category key below bar
            g2.setColor(new Color(160, 175, 200));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String key = entry.getKey();
            if (key.length() > 9) key = key.substring(0, 7) + "..";
            g2.drawString(key, x + (barW - fm.stringWidth(key)) / 2, marginT + chartH + 18);

            idx++;
        }

        g2.dispose();
    }
}
