package com.industrialinspection.service;

import com.industrialinspection.ai.AIInspectionService;
import com.industrialinspection.ai.PredictionResult;
import com.industrialinspection.ai.SimulatedAIInspectionService;
import com.industrialinspection.dao.AIPredictionDAO;
import com.industrialinspection.dao.AIPredictionDAOImpl;
import com.industrialinspection.exception.AIInspectionException;
import com.industrialinspection.model.AIPrediction;
import com.industrialinspection.model.ProductImage;
import com.industrialinspection.util.ThreadPoolManager;

import java.io.File;
import java.util.concurrent.Future;

/**
 * Orchestrator for Visual AI predictions.
 * Employs polymorphism: uses AIInspectionService interface which can be swapped
 * for future external Computer Vision inference APIs.
 */
public class AIPredictionService {

    private final AIInspectionService aiInspectionEngine;
    private final AIPredictionDAO predictionDAO;

    public AIPredictionService() {
        this(new SimulatedAIInspectionService(), new AIPredictionDAOImpl());
    }

    public AIPredictionService(AIInspectionService aiInspectionEngine, AIPredictionDAO predictionDAO) {
        this.aiInspectionEngine = aiInspectionEngine;
        this.predictionDAO = predictionDAO;
    }

    /**
     * Synchronous visual AI analysis.
     */
    public AIPrediction runPrediction(ProductImage image, Integer userId) throws AIInspectionException {
        File imageFile = new File(image.getFilePath());
        if (!imageFile.exists()) {
            throw new AIInspectionException("Image file does not exist on disk: " + image.getFilePath());
        }

        PredictionResult res = aiInspectionEngine.analyzeImage(imageFile);

        AIPrediction pred = new AIPrediction();
        pred.setImageId(image.getImageId());
        pred.setPredictedLabel(res.getPrediction());
        pred.setConfidenceScore(res.getConfidenceScore());
        pred.setDefectType(res.getDefectType());
        pred.setSeverity(res.getSeverity());
        pred.setDefectLocation(res.getDefectLocation());
        pred.setBoundingBoxX(res.getBoundingBoxX());
        pred.setBoundingBoxY(res.getBoundingBoxY());
        pred.setBoundingBoxW(res.getBoundingBoxW());
        pred.setBoundingBoxH(res.getBoundingBoxH());
        pred.setProcessingTimeMs(res.getProcessingTimeMs());
        pred.setRawResponseJson(res.getRawResponseJson());

        predictionDAO.addPrediction(pred);

        AuditService.getInstance().logAction(userId, "AI_PREDICTION_GENERATED", "ai_predictions", pred.getPredictionId(),
                "AI classified image #" + image.getImageId() + " as " + pred.getPredictedLabel() + 
                " (Confidence: " + String.format("%.1f%%", pred.getConfidenceScore()) + ")");

        return pred;
    }

    /**
     * Asynchronous AI analysis leveraging Java multithreading and Future.
     */
    public Future<AIPrediction> runPredictionAsync(ProductImage image, Integer userId) {
        return ThreadPoolManager.getInstance().submitImageTask(() -> runPrediction(image, userId));
    }

    public AIPrediction getPredictionById(int id) {
        return predictionDAO.getPredictionById(id);
    }

    public AIPrediction getPredictionByImageId(int imageId) {
        return predictionDAO.getPredictionByImageId(imageId);
    }
}
