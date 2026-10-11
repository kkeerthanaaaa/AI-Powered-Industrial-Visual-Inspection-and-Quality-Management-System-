package com.industrialinspection.dao;

import com.industrialinspection.model.AIPrediction;
import java.util.List;

public interface AIPredictionDAO {
    void addPrediction(AIPrediction prediction);
    AIPrediction getPredictionById(int id);
    AIPrediction getPredictionByImageId(int imageId);
    List<AIPrediction> getAllPredictions();
    void deletePrediction(int id);
}
