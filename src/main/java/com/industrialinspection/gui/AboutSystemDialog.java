package com.industrialinspection.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 18. System Specifications, Company Profile & Compliance Dialog.
 */
public class AboutSystemDialog extends JDialog {

    public AboutSystemDialog(Window parent) {
        super(parent, "About Visual Inspection & Quality System", ModalityType.APPLICATION_MODAL);
        setSize(560, 480);
        setLocationRelativeTo(parent);

        JPanel content = new JPanel();
        content.setBackground(UITheme.BG_DARKEST);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("ABC PRECISION MANUFACTURING PVT. LTD.");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UITheme.TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("AI-Powered Industrial Visual Inspection & Quality Management System");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.ACCENT_CYAN);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 4)));
        content.add(sub);
        content.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridLayout(8, 2, 8, 8));

        card.add(createLabel("Application Version:")); card.add(createValue("1.0.0 Enterprise Release"));
        card.add(createLabel("Target Industry:")); card.add(createValue("Automotive & Aerospace Precision"));
        card.add(createLabel("Technology Stack:")); card.add(createValue("Pure Java SE 17+, Swing, JDBC, MySQL"));
        card.add(createLabel("Java VM Runtime:")); card.add(createValue(System.getProperty("java.runtime.version", "17.x")));
        card.add(createLabel("Host Operating System:")); card.add(createValue(System.getProperty("os.name", "Windows")));
        card.add(createLabel("Quality Compliance:")); card.add(createValue("ISO 9001:2015 & AS9100D"));
        card.add(createLabel("Inference Engine:")); card.add(createValue("Edge Computer Vision Simulator (v2.1)"));
        card.add(createLabel("Plant Location:")); card.add(createValue("Plant 3, Precision Machining Hub, Pune"));

        content.add(card);
        content.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton closeBtn = UITheme.createPrimaryButton("Close");
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dispose());
        content.add(closeBtn);

        setContentPane(content);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MUTED);
        return l;
    }

    private JLabel createValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_REGULAR);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }
}
