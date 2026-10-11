package com.industrialinspection.util;

import com.industrialinspection.exception.InvalidImageException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

/**
 * Image processing, verification, and file operations utility using pure Java AWT and ImageIO.
 */
public class ImageUtil {

    private static final String UPLOAD_DIR = "uploads/inspections";

    /**
     * Reads image dimensions using ImageIO.
     * Returns int[2] where [0]=width, [1]=height.
     */
    public static int[] getImageDimensions(File imageFile) throws InvalidImageException {
        try {
            BufferedImage bimg = ImageIO.read(imageFile);
            if (bimg == null) {
                throw new InvalidImageException("Could not decode image file: " + imageFile.getName());
            }
            return new int[]{bimg.getWidth(), bimg.getHeight()};
        } catch (IOException e) {
            throw new InvalidImageException("Failed to read image dimensions: " + e.getMessage(), e);
        }
    }

    /**
     * Copies and safely renames the uploaded inspection file into the uploads storage repository.
     */
    public static File saveInspectionImage(File sourceFile, int productId, int batchId) throws InvalidImageException {
        try {
            Path targetDir = Paths.get(UPLOAD_DIR);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String ext = getFileExtension(sourceFile);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String safeFileName = "INSP_P" + productId + "_B" + batchId + "_" + timestamp + "." + ext;

            Path targetPath = targetDir.resolve(safeFileName);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toFile();
        } catch (IOException e) {
            throw new InvalidImageException("Failed to save inspection image: " + e.getMessage(), e);
        }
    }

    /**
     * Resizes a BufferedImage proportionally to fit within target maxWidth and maxHeight.
     */
    public static BufferedImage createThumbnail(BufferedImage original, int maxWidth, int maxHeight) {
        int origW = original.getWidth();
        int origH = original.getHeight();

        double ratio = Math.min((double) maxWidth / origW, (double) maxHeight / origH);
        int targetW = Math.max(1, (int) (origW * ratio));
        int targetH = Math.max(1, (int) (origH * ratio));

        BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(original, 0, 0, targetW, targetH, null);
        g2.dispose();
        return scaled;
    }

    /**
     * Overlays a bounding box, confidence tag, and defect classification on top of the image.
     */
    public static BufferedImage overlayDefectBoundingBox(BufferedImage source, int x, int y, int w, int h, 
                                                         String defectLabel, double confidence, Color boxColor) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = copy.createGraphics();
        g2.drawImage(source, 0, 0, null);

        if (w > 0 && h > 0) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(boxColor);
            g2.setStroke(new BasicStroke(4.0f));
            g2.drawRect(x, y, w, h);

            // Shaded overlay inside bounding box
            g2.setColor(new Color(boxColor.getRed(), boxColor.getGreen(), boxColor.getBlue(), 40));
            g2.fillRect(x, y, w, h);

            // Badge text banner
            String tag = defectLabel + " (" + String.format("%.1f%%", confidence) + ")";
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            int tagW = fm.stringWidth(tag) + 16;
            int tagH = fm.getHeight() + 8;

            int badgeY = Math.max(0, y - tagH - 4);
            g2.setColor(boxColor);
            g2.fillRoundRect(x, badgeY, tagW, tagH, 6, 6);
            g2.setColor(Color.WHITE);
            g2.drawString(tag, x + 8, badgeY + fm.getAscent() + 2);
        }

        g2.dispose();
        return copy;
    }

    /**
     * Synthesizes a realistic sample precision component inspection image if none exists.
     */
    public static File createSampleIndustrialImage(String filename, String componentName, boolean hasDefect) {
        try {
            Path targetDir = Paths.get(UPLOAD_DIR);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            File outFile = targetDir.resolve(filename).toFile();
            if (outFile.exists()) {
                return outFile;
            }

            int w = 800;
            int h = 600;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Metallic gradient background
            GradientPaint gp = new GradientPaint(0, 0, new Color(30, 35, 45), w, h, new Color(15, 18, 24));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            // Optical Inspection Grid Overlay
            g2.setColor(new Color(255, 255, 255, 15));
            for (int i = 0; i < w; i += 40) g2.drawLine(i, 0, i, h);
            for (int j = 0; j < h; j += 40) g2.drawLine(0, j, w, j);

            // Render Component disc / casing
            g2.setColor(new Color(80, 90, 110));
            g2.fillOval(150, 80, 500, 440);
            g2.setColor(new Color(130, 145, 170));
            g2.setStroke(new BasicStroke(6f));
            g2.drawOval(150, 80, 500, 440);

            // Inner bore / rotor hub
            g2.setColor(new Color(40, 48, 60));
            g2.fillOval(300, 200, 200, 200);
            g2.setColor(new Color(170, 185, 210));
            g2.drawOval(300, 200, 200, 200);

            // Bolt holes
            g2.setColor(new Color(20, 25, 35));
            int[][] holes = {{400, 130}, {400, 470}, {230, 300}, {570, 300}, {280, 180}, {520, 180}, {280, 420}, {520, 420}};
            for (int[] pos : holes) {
                g2.fillOval(pos[0] - 14, pos[1] - 14, 28, 28);
                g2.setColor(new Color(100, 120, 140));
                g2.drawOval(pos[0] - 14, pos[1] - 14, 28, 28);
                g2.setColor(new Color(20, 25, 35));
            }

            // If defect is simulated on test sample
            if (hasDefect) {
                g2.setColor(new Color(240, 60, 60));
                g2.setStroke(new BasicStroke(3f));
                // Jagged scratch/crack
                g2.drawLine(480, 130, 520, 160);
                g2.drawLine(520, 160, 510, 190);
                g2.drawLine(510, 190, 550, 220);
            }

            // HUD Metadata
            g2.setColor(new Color(0, 210, 255));
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2.drawString("ABC PRECISION MFG - OPTICAL CELL 01", 30, 35);
            g2.drawString("COMPONENT: " + componentName, 30, 55);
            g2.drawString("TIME: " + new Date().toString(), 30, 570);

            g2.dispose();
            ImageIO.write(img, "PNG", outFile);
            return outFile;
        } catch (IOException e) {
            return null;
        }
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? "png" : name.substring(dot + 1).toLowerCase();
    }
}
