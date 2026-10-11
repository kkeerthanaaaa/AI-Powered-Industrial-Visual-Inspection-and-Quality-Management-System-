package com.industrialinspection.dao;

import com.industrialinspection.model.DefectType;
import java.util.List;

public interface DefectTypeDAO {
    void addDefectType(DefectType defectType);
    DefectType getDefectTypeById(int id);
    DefectType getDefectTypeByCode(String code);
    List<DefectType> getAllDefectTypes();
    void updateDefectType(DefectType defectType);
    void deleteDefectType(int id);
}
