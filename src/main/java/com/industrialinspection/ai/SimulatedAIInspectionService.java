package com.industrialinspection.ai;

import com.industrialinspection.exception.AIInspectionException;
import com.industrialinspection.model.enums.DefectLocation;
import com.industrialinspection.model.enums.SeverityLevel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Simulated AI Inspection Engine simulating edge Computer Vision inference.
 * Analyzes image pixel matrices, dimensions, and file features to produce
 * realistic industrial defect predictions and bounding boxes.
 */
public class SimulatedAIInspectionService implements AIInspectionService {

    private static final String[] DEFECT_CANDIDATES = {
        "DEFECT_FREE",
        "SCRATCH",
        "CRACK",
        "DENT",
        "DISCOLORATION",
        "SURFACE_DAMAGE",
        "MISSING_COMPONENT",
        "DEFECT_FREE"
    };

    @Override
    public PredictionResult analyzeImage(File image) throws AIInspectionException {
        return analyzeImage(image, "GENERAL_COMPONENT");
    }

    @Override
    public PredictionResult analyzeImage(File image, String productCategory) throws AIInspectionException {
        if (image == null || !image.exists() || !image.isFile()) {
            throw new AIInspectionException("Image file does not exist or cannot be accessed.");
        }

        long startTime = System.currentTimeMillis();

        try {
            BufferedImage bimg = ImageIO.read(image);
            int width = bimg != null ? bimg.getWidth() : 800;
            int height = bimg != null ? bimg.getHeight() : 600;

            // Generate deterministic but realistic defect outcome based on image name and file size
            String filename = image.getName().toLowerCase();
            String defectLabel;
            SeverityLevel severity;
            DefectLocation location;
            double confidence;

            if (filename.contains("crack")) {
                defectLabel = "CRACK";
                severity = SeverityLevel.CRITICAL;
                location = DefectLocation.TOP_RIGHT;
                confidence = 94.60;
            } else if (filename.contains("scratch")) {
                defectLabel = "SCRATCH";
                severity = SeverityLevel.MEDIUM;
                location = DefectLocation.TOP_LEFT;
                confidence = 92.40;
            } else if (filename.contains("dent")) {
                defectLabel = "DENT";
                severity = SeverityLevel.HIGH;
                location = DefectLocation.CENTER;
                confidence = 91.80;
            } else if (filename.contains("good") || filename.contains("pass") || filename.contains("defect_free")) {
                defectLabel = "DEFECT_FREE";
                severity = SeverityLevel.LOW;
                location = DefectLocation.CENTER;
                confidence = 98.50;
            } else {
                // Heuristic using file size and hash
                long hash = Math.abs(filename.hashCode() ^ image.length());
                int idx = (int) (hash % DEFECT_CANDIDATES.length);
                defectLabel = DEFECT_CANDIDATES[idx];

                if ("DEFECT_FREE".equals(defectLabel)) {
                    severity = SeverityLevel.LOW;
                    location = DefectLocation.CENTER;
                    confidence = 96.0 + (hash % 380) / 100.0; // 96.0 - 99.8%
                } else {
                    confidence = 88.0 + (hash % 1000) / 100.0; // 88.0 - 98.0%
                    severity = determineSeverity(defectLabel);
                    location = DefectLocation.values()[(int) ((hash / 7) % DefectLocation.values().length)];
                }
            }

            // Bounding box calculations
            int bboxX = 0, bboxY = 0, bboxW = 0, bboxH = 0;
            if (!"DEFECT_FREE".equalsIgnoreCase(defectLabel)) {
                bboxW = Math.max(80, (int) (width * 0.22));
                bboxH = Math.max(60, (int) (height * 0.18));

                switch (location) {
                    case TOP_LEFT -> { bboxX = (int) (width * 0.15); bboxY = (int) (height * 0.15); }
                    case TOP_RIGHT -> { bboxX = (int) (width * 0.65); bboxY = (int) (height * 0.15); }
                    case BOTTOM_LEFT -> { bboxX = (int) (width * 0.15); bboxY = (int) (height * 0.65); }
                    case BOTTOM_RIGHT -> { bboxX = (int) (width * 0.65); bboxY = (int) (height * 0.65); }
                    case PERIMETER -> { bboxX = (int) (width * 0.40); bboxY = (int) (height * 0.10); bboxW = (int) (width * 0.45); }
                    case ENTIRE_SURFACE -> { bboxX = (int) (width * 0.1); bboxY = (int) (height * 0.1); bboxW = (int) (width * 0.8); bboxH = (int) (height * 0.8); }
                    default -> { bboxX = (int) (width * 0.40); bboxY = (int) (height * 0.40); }
                }
            }

            // Simulated inference latency (35ms - 65ms)
            long elapsed = System.currentTimeMillis() - startTime;
            int simulatedDelay = (int) (35 + (Math.abs(image.length()) % 30));
            int processingTime = Math.max(35, (int) elapsed + simulatedDelay);

            String rawJson = String.format(
                "{\"prediction\":\"%s\",\"confidence\":%.2f,\"severity\":\"%s\",\"location\":\"%s\"," +
                "\"coords\":{\"x\":%d,\"y\":%d,\"w\":%d,\"h\":%d},\"latency_ms\":%d,\"model\":\"SimulatedVision-V2\"}",
                defectLabel, confidence, severity, location, bboxX, bboxY, bboxW, bboxH, processingTime
            );

            return new PredictionResult(
                defectLabel,
                confidence,
                defectLabel,
                severity,
                location,
                bboxX, bboxY, bboxW, bboxH,
                processingTime,
                rawJson
            );

        } catch (IOException e) {
            throw new AIInspectionException("Image reading error during simulated inspection: " + e.getMessage(), e);
        }
    }

    private SeverityLevel determineSeverity(String defect) {
        return switch (defect) {
            case "CRACK", "MISSING_COMPONENT" -> SeverityLevel.CRITICAL;
            case "DENT", "SURFACE_DAMAGE" -> SeverityLevel.HIGH;
            case "SCRATCH", "MISALIGNMENT" -> SeverityLevel.MEDIUM;
            case "DISCOLORATION" -> SeverityLevel.LOW;
            default -> SeverityLevel.MEDIUM;
        };
    }
}
