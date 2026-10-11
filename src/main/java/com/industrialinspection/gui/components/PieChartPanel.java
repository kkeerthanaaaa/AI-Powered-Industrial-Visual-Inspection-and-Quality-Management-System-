package com.industrialinspection.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Pure Java2D Pie Chart Component.
 * Zero external libraries; rendered with Graphics2D arc primitives.
 */
public class PieChartPanel extends JPanel {

    private final Map<String, Integer> data = new LinkedHashMap<>();
    private String title = "Defect Category Share";

    private final Color[] palette = {
        new Color(0, 168, 255),
        new Color(255, 75, 75),
        new Color(255, 170, 0),
        new Color(0, 210, 140),
        new Color(156, 39, 176),
        new Color(233, 30, 99),
        new Color(0, 188, 212)
    };

    public PieChartPanel() {
        setBackground(new Color(24, 28, 38));
        setPreferredSize(new Dimension(360, 240));
    }

    public PieChartPanel(String title, Map<String, Integer> initialData) {
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

        // Title
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

        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) total = 1;

        int diameter = Math.min(w / 2 - 30, h - 60);
        int pieX = 20;
        int pieY = (h - diameter) / 2 + 10;

        double curAngle = 0.0;
        int colorIdx = 0;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            double angle = (entry.getValue() * 360.0) / total;
            Color c = palette[colorIdx % palette.length];
            g2.setColor(c);
            g2.fillArc(pieX, pieY, diameter, diameter, (int) Math.round(curAngle), (int) Math.ceil(angle));
            curAngle += angle;
            colorIdx++;
        }

        // Donut hole for modern industrial look
        int holeDia = (int) (diameter * 0.50);
        int holeX = pieX + (diameter - holeDia) / 2;
        int holeY = pieY + (diameter - holeDia) / 2;
        g2.setColor(getBackground());
        g2.fillOval(holeX, holeY, holeDia, holeDia);

        // Center total counter
        g2.setColor(new Color(240, 245, 255));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String totStr = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totStr, holeX + (holeDia - fm.stringWidth(totStr)) / 2, holeY + holeDia / 2 + 5);

        // Render Legend on the right
        int legendX = pieX + diameter + 25;
        int legendY = 48;
        colorIdx = 0;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            Color c = palette[colorIdx % palette.length];
            g2.setColor(c);
            g2.fillRoundRect(legendX, legendY, 10, 10, 3, 3);

            g2.setColor(new Color(200, 210, 230));
            double pct = (entry.getValue() * 100.0) / total;
            String text = String.format("%s (%.0f%%)", entry.getKey(), pct);
            if (text.length() > 18) text = text.substring(0, 16) + "..";
            g2.drawString(text, legendX + 16, legendY + 9);

            legendY += 20;
            colorIdx++;
            if (legendY > h - 15) break;
        }

        g2.dispose();
    }
}
