package com.industrialinspection.gui;

import javax.swing.*;
import java.awt.*;

/**
 * 1. Industrial Splash Screen with animated startup tasks.
 */
public class SplashScreen extends JWindow {

    private final JProgressBar progressBar;
    private final JLabel statusLabel;

    public SplashScreen() {
        setSize(580, 360);
        setLocationRelativeTo(null);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(18, 23, 33), 580, 360, new Color(10, 13, 19));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Grid pattern
                g2.setColor(new Color(255, 255, 255, 10));
                for (int i = 0; i < getWidth(); i += 30) g2.drawLine(i, 0, i, getHeight());
                for (int j = 0; j < getHeight(); j += 30) g2.drawLine(0, j, getWidth(), j);

                // Industrial branding banner
                g2.setColor(UITheme.ACCENT_CYAN);
                g2.fillRoundRect(35, 35, 12, 54, 4, 4);

                g2.dispose();
            }
        };
        content.setLayout(null);
        content.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_BLUE, 1));

        JLabel titleLabel = new JLabel("ABC PRECISION MANUFACTURING");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.TEXT_MAIN);
        titleLabel.setBounds(60, 32, 480, 30);
        content.add(titleLabel);

        JLabel subLabel = new JLabel("AI-POWERED VISUAL INSPECTION & QUALITY MANAGEMENT");
        subLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        subLabel.setForeground(UITheme.ACCENT_CYAN);
        subLabel.setBounds(60, 62, 480, 20);
        content.add(subLabel);

        JLabel descLabel = new JLabel("<html>Aerospace & Automotive Optical Defect Analysis System<br>Version 1.0.0 Enterprise | ISO 9001:2015 Compliant</html>");
        descLabel.setFont(UITheme.FONT_REGULAR);
        descLabel.setForeground(UITheme.TEXT_MUTED);
        descLabel.setBounds(60, 120, 460, 50);
        content.add(descLabel);

        statusLabel = new JLabel("Initializing system modules...");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(new Color(180, 195, 215));
        statusLabel.setBounds(60, 248, 460, 20);
        content.add(statusLabel);

        progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(60, 275, 460, 10);
        progressBar.setForeground(UITheme.ACCENT_BLUE);
        progressBar.setBackground(new Color(30, 36, 50));
        progressBar.setBorderPainted(false);
        content.add(progressBar);

        JLabel footer = new JLabel("Licensed to ABC Precision Manufacturing Pvt. Ltd. (Pune Hub)");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(110, 125, 145));
        footer.setBounds(60, 310, 460, 20);
        content.add(footer);

        setContentPane(content);
    }

    public void launch(Runnable onComplete) {
        setVisible(true);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String[] steps = {
                    "Loading JDBC & Database driver...",
                    "Connecting to industrial_inspection schema...",
                    "Loading master defect taxonomy...",
                    "Initializing Simulated AI Computer Vision engine...",
                    "Verifying camera mount hardware CAM-OPTICAL-01...",
                    "Starting background audit logging service...",
                    "System initialized. Launching operator interface..."
                };

                for (int i = 0; i <= 100; i += 2) {
                    Thread.sleep(25);
                    int stepIdx = Math.min(steps.length - 1, (i * steps.length) / 100);
                    final String msg = steps[stepIdx];
                    final int val = i;
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(val);
                        statusLabel.setText(msg);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                dispose();
                if (onComplete != null) {
                    SwingUtilities.invokeLater(onComplete);
                }
            }
        };
        worker.execute();
    }
}
