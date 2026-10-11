package com.industrialinspection.dao;

import com.industrialinspection.model.InspectionRecord;
import java.util.List;

public interface InspectionDAO {
    void addInspection(InspectionRecord record);
    InspectionRecord getInspectionById(int id);
    List<InspectionRecord> getAllInspections();
    List<InspectionRecord> getInspectionsByBatchId(int batchId);
    List<InspectionRecord> getInspectionsByProductId(int productId);
    void updateInspection(InspectionRecord record);
    void deleteInspection(int id);
    List<InspectionRecord> searchInspections(String keyword, String decision);
    int getTotalInspectionsCount();
    int getPassedInspectionsCount();
    int getFailedInspectionsCount();
    int getPendingReviewCount();
}
