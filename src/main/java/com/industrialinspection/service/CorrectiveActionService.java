package com.industrialinspection.service;

import com.industrialinspection.dao.CorrectiveActionDAO;
import com.industrialinspection.dao.CorrectiveActionDAOImpl;
import com.industrialinspection.exception.ValidationException;
import com.industrialinspection.model.CorrectiveAction;
import com.industrialinspection.model.enums.ActionStatus;
import com.industrialinspection.util.CSVUtil;
import com.industrialinspection.util.DateTimeUtil;
import com.industrialinspection.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class CorrectiveActionService {

    private final CorrectiveActionDAO correctiveActionDAO;

    public CorrectiveActionService() {
        this(new CorrectiveActionDAOImpl());
    }

    public CorrectiveActionService(CorrectiveActionDAO correctiveActionDAO) {
        this.correctiveActionDAO = correctiveActionDAO;
    }

    public void createCorrectiveAction(CorrectiveAction action, Integer userId) throws ValidationException {
        ValidationUtil.validatePositive(action.getInspectionId(), "Inspection ID");
        ValidationUtil.validateNotEmpty(action.getRootCause(), "Root Cause");
        ValidationUtil.validateNotEmpty(action.getCorrectiveAction(), "Corrective Action");
        ValidationUtil.validateNotEmpty(action.getResponsiblePerson(), "Responsible Person");

        if (action.getTargetDate() == null) {
            action.setTargetDate(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000L)); // 7 days ahead
        }

        correctiveActionDAO.addCorrectiveAction(action);
        AuditService.getInstance().logAction(userId, "CORRECTIVE_ACTION_CREATED", "corrective_actions", action.getActionId(),
                "Issued CAPA #" + action.getActionId() + " for Inspection #" + action.getInspectionId() + " assigned to " + action.getResponsiblePerson());
    }

    public void updateStatus(int actionId, ActionStatus status, String remarks, Integer userId) {
        CorrectiveAction ca = correctiveActionDAO.getCorrectiveActionById(actionId);
        if (ca != null) {
            ca.setStatus(status);
            if (remarks != null && !remarks.isEmpty()) {
                ca.setRemarks(remarks);
            }
            if (status == ActionStatus.COMPLETED || status == ActionStatus.VERIFIED) {
                ca.setCompletionDate(new Date(System.currentTimeMillis()));
            }
            correctiveActionDAO.updateCorrectiveAction(ca);
            AuditService.getInstance().logAction(userId, "CORRECTIVE_ACTION_UPDATED", "corrective_actions", actionId,
                    "CAPA #" + actionId + " status updated to " + status);
        }
    }

    public CorrectiveAction getActionById(int id) {
        return correctiveActionDAO.getCorrectiveActionById(id);
    }

    public List<CorrectiveAction> getAllActions() {
        return correctiveActionDAO.getAllCorrectiveActions();
    }

    public List<CorrectiveAction> getActionsByInspectionId(int inspectionId) {
        return correctiveActionDAO.getCorrectiveActionsByInspectionId(inspectionId);
    }

    public void exportActionsToCSV(File destination) throws IOException {
        List<CorrectiveAction> list = getAllActions();
        List<String[]> csv = new ArrayList<>();
        csv.add(new String[]{"Action ID", "Inspection ID", "Product", "Batch Number", "Root Cause", "Corrective Action", "Responsible Person", "Target Date", "Completion Date", "Status", "Remarks"});

        for (CorrectiveAction ca : list) {
            csv.add(new String[]{
                String.valueOf(ca.getActionId()),
                String.valueOf(ca.getInspectionId()),
                ca.getProductName() != null ? ca.getProductName() : "",
                ca.getBatchNumber() != null ? ca.getBatchNumber() : "",
                ca.getRootCause(),
                ca.getCorrectiveAction(),
                ca.getResponsiblePerson(),
                DateTimeUtil.formatDate(ca.getTargetDate()),
                DateTimeUtil.formatDate(ca.getCompletionDate()),
                ca.getStatus().name(),
                ca.getRemarks() != null ? ca.getRemarks() : ""
            });
        }
        CSVUtil.writeCSV(destination, csv);
    }
}
