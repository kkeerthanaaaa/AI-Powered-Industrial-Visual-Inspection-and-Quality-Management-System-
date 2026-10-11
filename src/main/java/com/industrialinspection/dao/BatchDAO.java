package com.industrialinspection.dao;

import com.industrialinspection.model.ProductionBatch;
import java.util.List;

public interface BatchDAO {
    void addBatch(ProductionBatch batch);
    ProductionBatch getBatchById(int id);
    ProductionBatch getBatchByNumber(String batchNumber);
    List<ProductionBatch> getAllBatches();
    void updateBatch(ProductionBatch batch);
    void deleteBatch(int id);
    List<ProductionBatch> searchBatches(String keyword, String status);
}
