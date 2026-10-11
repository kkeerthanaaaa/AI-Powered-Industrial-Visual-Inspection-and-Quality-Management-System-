package com.industrialinspection.ai;

import com.industrialinspection.exception.AIInspectionException;
import java.io.File;

/**
 * Interface for AI Visual Inspection service.
 * Demonstrates Abstraction and Polymorphism: can be implemented by
 * SimulatedAIInspectionService, or future external Edge/Cloud Computer Vision APIs.
 */
public interface AIInspectionService {

    /**
     * Inspects a manufactured component image file and produces visual prediction results.
     *
     * @param image File handle to the optical component image
     * @return PredictionResult containing defect classification, confidence, bounding boxes
     * @throws AIInspectionException if image reading or inference fails
     */
    PredictionResult analyzeImage(File image) throws AIInspectionException;

    /**
     * Inspects an image given product metadata hint.
     */
    PredictionResult analyzeImage(File image, String productCategory) throws AIInspectionException;
}
