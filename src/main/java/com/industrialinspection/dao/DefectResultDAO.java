package com.industrialinspection.dao;

import com.industrialinspection.model.DefectResult;
import java.util.List;

public interface DefectResultDAO {
    void addDefectResult(DefectResult defectResult);
    DefectResult getDefectResultById(int id);
    List<DefectResult> getDefectResultsByInspectionId(int inspectionId);
    List<DefectResult> getAllDefectResults();
    void deleteDefectResult(int id);
}
