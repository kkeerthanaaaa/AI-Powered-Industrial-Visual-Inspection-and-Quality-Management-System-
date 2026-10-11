package com.industrialinspection.gui;

import com.industrialinspection.model.AIPrediction;
import com.industrialinspection.model.ProductImage;
import com.industrialinspection.util.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 8. AI Prediction & Computer Vision Analysis Viewer Dialog.
 */
public class AIPredictionViewerDialog extends JDialog {

    public AIPredictionViewerDialog(Window parent, ProductImage image, AIPrediction prediction) {
        super(parent, "AI Computer Vision Inference Results - Image #" + (image != null ? image.getImageId() : "N/A"), ModalityType.APPLICATION_MODAL);
        setSize(820, 620);
        setLocationRelativeTo(parent);

        JPanel main = new JPanel(new BorderLayout(14, 14));
        main.setBackground(UITheme.BG_DARKEST);
        main.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(UITheme.BG_CARD);
        banner.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel("INSPECTION INFERENCE ENGINE: " + (prediction != null ? prediction.getPredictedLabel() : "PENDING"));
        titleLbl.setFont(UITheme.FONT_HEADER_LARGE);
        Color statusColor = (prediction != null && prediction.isDefectFree()) ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED;
        titleLbl.setForeground(statusColor);

        JLabel confLbl = new JLabel(prediction != null ? 
                String.format("Confidence: %.2f%% | Processing Time: %d ms | Severity: %s",
                        prediction.getConfidenceScore(), prediction.getProcessingTimeMs(), prediction.getSeverity()) : "");
        confLbl.setFont(UITheme.FONT_BOLD);
        confLbl.setForeground(UITheme.TEXT_MUTED);

        banner.add(titleLbl, BorderLayout.WEST);
        banner.add(confLbl, BorderLayout.EAST);
        main.add(banner, BorderLayout.NORTH);

        // Center split: Image overlay on left, Details on right
        JPanel center = new JPanel(new GridLayout(1, 2, 16, 16));
        center.setBackground(UITheme.BG_DARKEST);

        JLabel imgLabel = new JLabel("Image loading...", SwingConstants.CENTER);
        imgLabel.setBackground(new Color(20, 24, 32));
        imgLabel.setOpaque(true);
        imgLabel.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        if (image != null && image.getFilePath() != null) {
            File imgFile = new File(image.getFilePath());
            if (imgFile.exists()) {
                try {
                    BufferedImage raw = ImageIO.read(imgFile);
                    if (raw != null) {
                        BufferedImage rendered;
                        if (prediction != null && !prediction.isDefectFree() && prediction.getBoundingBoxW() > 0) {
                            rendered = ImageUtil.overlayDefectBoundingBox(
                                    raw,
                                    prediction.getBoundingBoxX(),
                                    prediction.getBoundingBoxY(),
                                    prediction.getBoundingBoxW(),
                                    prediction.getBoundingBoxH(),
                                    prediction.getPredictedLabel(),
                                    prediction.getConfidenceScore(),
                                    UITheme.ACCENT_RED
                            );
                        } else {
                            rendered = raw;
                        }
                        BufferedImage thumb = ImageUtil.createThumbnail(rendered, 380, 420);
                        imgLabel.setIcon(new ImageIcon(thumb));
                        imgLabel.setText("");
                    }
                } catch (Exception ignored) {}
            }
        }
        center.add(imgLabel);

        // Details Card
        JPanel detailsCard = UITheme.createCardPanel();
        detailsCard.setLayout(new BoxLayout(detailsCard, BoxLayout.Y_AXIS));

        detailsCard.add(createDetailItem("Inference Status:", prediction != null ? (prediction.isDefectFree() ? "PASS (NO DEFECT DETECTED)" : "ANOMALY / DEFECT DETECTED") : "N/A"));
        detailsCard.add(createDetailItem("Defect Classification:", prediction != null ? prediction.getDefectType() : "N/A"));
        detailsCard.add(createDetailItem("Confidence Score:", prediction != null ? String.format("%.2f%%", prediction.getConfidenceScore()) : "N/A"));
        detailsCard.add(createDetailItem("Severity Rating:", prediction != null ? prediction.getSeverity().name() : "N/A"));
        detailsCard.add(createDetailItem("Defect Region:", prediction != null ? prediction.getDefectLocation().getLabel() : "N/A"));
        detailsCard.add(createDetailItem("Bounding Box Coordinates:", prediction != null ? 
                String.format("[X=%d, Y=%d, W=%d, H=%d]", prediction.getBoundingBoxX(), prediction.getBoundingBoxY(), prediction.getBoundingBoxW(), prediction.getBoundingBoxH()) : "N/A"));
        detailsCard.add(createDetailItem("Simulated Latency:", prediction != null ? (prediction.getProcessingTimeMs() + " ms") : "N/A"));

        detailsCard.add(Box.createRigidArea(new Dimension(0, 8)));
        JLabel rawTitle = new JLabel("Raw Edge Inference JSON Response:");
        rawTitle.setFont(UITheme.FONT_BOLD);
        rawTitle.setForeground(UITheme.ACCENT_CYAN);
        detailsCard.add(rawTitle);

        JTextArea jsonArea = new JTextArea();
        jsonArea.setEditable(false);
        jsonArea.setFont(UITheme.FONT_MONO);
        jsonArea.setBackground(UITheme.BG_INPUT);
        jsonArea.setForeground(UITheme.TEXT_MAIN);
        jsonArea.setText(prediction != null ? prediction.getRawResponseJson() : "{}");
        jsonArea.setLineWrap(true);
        jsonArea.setWrapStyleWord(true);
        JScrollPane jsonScroll = new JScrollPane(jsonArea);
        jsonScroll.setPreferredSize(new Dimension(340, 120));
        detailsCard.add(jsonScroll);

        center.add(detailsCard);
        main.add(center, BorderLayout.CENTER);

        // Bottom Dismiss
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(UITheme.BG_DARKEST);
        JButton closeBtn = UITheme.createPrimaryButton("Dismiss Viewer");
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JPanel createDetailItem(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 2));
        row.setBackground(UITheme.BG_CARD);
        row.setMaximumSize(new Dimension(500, 26));

        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_REGULAR);
        v.setForeground(UITheme.TEXT_MAIN);

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }
}
