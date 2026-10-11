package com.industrialinspection.service;

import com.industrialinspection.dao.*;
import com.industrialinspection.exception.ValidationException;
import com.industrialinspection.model.DefectResult;
import com.industrialinspection.model.DefectType;
import com.industrialinspection.model.enums.DefectLocation;
import com.industrialinspection.model.enums.SeverityLevel;
import com.industrialinspection.util.CSVUtil;
import com.industrialinspection.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DefectService {

    private final DefectTypeDAO defectTypeDAO;
    private final DefectResultDAO defectResultDAO;

    public DefectService() {
        this(new DefectTypeDAOImpl(), new DefectResultDAOImpl());
    }

    public DefectService(DefectTypeDAO defectTypeDAO, DefectResultDAO defectResultDAO) {
        this.defectTypeDAO = defectTypeDAO;
        this.defectResultDAO = defectResultDAO;
    }

    public void addDefectType(DefectType defectType, Integer userId) throws ValidationException {
        ValidationUtil.validateNotEmpty(defectType.getDefectCode(), "Defect Code");
        ValidationUtil.validateNotEmpty(defectType.getDefectName(), "Defect Name");
        ValidationUtil.validateNotEmpty(defectType.getCategory(), "Category");

        defectTypeDAO.addDefectType(defectType);
        AuditService.getInstance().logAction(userId, "DEFECT_TYPE_CREATED", "defect_types", defectType.getDefectTypeId(),
                "Created master defect catalog entry: " + defectType.getDefectCode() + " (" + defectType.getDefectName() + ")");
    }

    public void updateDefectType(DefectType defectType, Integer userId) throws ValidationException {
        ValidationUtil.validateNotEmpty(defectType.getDefectName(), "Defect Name");
        defectTypeDAO.updateDefectType(defectType);
        AuditService.getInstance().logAction(userId, "DEFECT_TYPE_UPDATED", "defect_types", defectType.getDefectTypeId(),
                "Updated defect type: " + defectType.getDefectCode());
    }

    public void deleteDefectType(int id, Integer userId) {
        DefectType dt = defectTypeDAO.getDefectTypeById(id);
        defectTypeDAO.deleteDefectType(id);
        if (dt != null) {
            AuditService.getInstance().logAction(userId, "DEFECT_TYPE_DELETED", "defect_types", id,
                    "Deleted defect type: " + dt.getDefectCode());
        }
    }

    public List<DefectType> getAllDefectTypes() {
        return defectTypeDAO.getAllDefectTypes();
    }

    public DefectType getDefectTypeById(int id) {
        return defectTypeDAO.getDefectTypeById(id);
    }

    public DefectType getDefectTypeByCode(String code) {
        return defectTypeDAO.getDefectTypeByCode(code);
    }

    public DefectResult recordDefectResult(int inspectionId, int defectTypeId, SeverityLevel severity, 
                                           DefectLocation location, int x, int y, int w, int h, 
                                           String comments, Integer userId) {
        DefectResult dr = new DefectResult();
        dr.setInspectionId(inspectionId);
        dr.setDefectTypeId(defectTypeId);
        dr.setSeverity(severity != null ? severity : SeverityLevel.MEDIUM);
        dr.setLocation(location != null ? location : DefectLocation.CENTER);
        dr.setDefectX(x);
        dr.setDefectY(y);
        dr.setDefectW(w);
        dr.setDefectH(h);
        dr.setComments(comments);

        defectResultDAO.addDefectResult(dr);
        AuditService.getInstance().logAction(userId, "DEFECT_RECORDED", "defect_results", dr.getDefectResultId(),
                "Logged defect result [" + dr.getSeverity() + " at " + dr.getLocation() + "] for Inspection #" + inspectionId);
        return dr;
    }

    public List<DefectResult> getDefectResultsByInspectionId(int inspectionId) {
        return defectResultDAO.getDefectResultsByInspectionId(inspectionId);
    }

    public List<DefectResult> getAllDefectResults() {
        return defectResultDAO.getAllDefectResults();
    }

    public void exportDefectsToCSV(File destination) throws IOException {
        List<DefectResult> list = getAllDefectResults();
        List<String[]> csv = new ArrayList<>();
        csv.add(new String[]{"Defect Result ID", "Inspection ID", "Defect Code", "Defect Name", "Severity", "Location", "Comments", "Date"});

        for (DefectResult dr : list) {
            csv.add(new String[]{
                String.valueOf(dr.getDefectResultId()),
                String.valueOf(dr.getInspectionId()),
                dr.getDefectCode() != null ? dr.getDefectCode() : "N/A",
                dr.getDefectName() != null ? dr.getDefectName() : ("Defect #" + dr.getDefectTypeId()),
                dr.getSeverity().name(),
                dr.getLocation().name(),
                dr.getComments() != null ? dr.getComments() : "",
                dr.getCreatedAt() != null ? dr.getCreatedAt().toString() : ""
            });
        }
        CSVUtil.writeCSV(destination, csv);
    }
}
