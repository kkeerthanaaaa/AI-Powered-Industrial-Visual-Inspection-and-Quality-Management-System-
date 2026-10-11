package com.industrialinspection.model;

import com.industrialinspection.model.enums.ActionStatus;
import java.sql.Date;
import java.sql.Timestamp;

public class CorrectiveAction {
    private int actionId;
    private int inspectionId;
    private Integer defectResultId;
    private String rootCause;
    private String correctiveAction;
    private String responsiblePerson;
    private Date targetDate;
    private Date completionDate;
    private ActionStatus status;
    private String remarks;
    private Timestamp createdAt;

    // Helpers
    private String productName;
    private String batchNumber;

    public CorrectiveAction() {
        this.status = ActionStatus.OPEN;
    }

    public CorrectiveAction(int actionId, int inspectionId, Integer defectResultId, 
                            String rootCause, String correctiveAction, String responsiblePerson, 
                            Date targetDate, ActionStatus status) {
        this();
        this.actionId = actionId;
        this.inspectionId = inspectionId;
        this.defectResultId = defectResultId;
        this.rootCause = rootCause;
        this.correctiveAction = correctiveAction;
        this.responsiblePerson = responsiblePerson;
        this.targetDate = targetDate;
        this.status = status;
    }

    public int getActionId() { return actionId; }
    public void setActionId(int actionId) { this.actionId = actionId; }

    public int getInspectionId() { return inspectionId; }
    public void setInspectionId(int inspectionId) { this.inspectionId = inspectionId; }

    public Integer getDefectResultId() { return defectResultId; }
    public void setDefectResultId(Integer defectResultId) { this.defectResultId = defectResultId; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getCorrectiveAction() { return correctiveAction; }
    public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }

    public String getResponsiblePerson() { return responsiblePerson; }
    public void setResponsiblePerson(String responsiblePerson) { this.responsiblePerson = responsiblePerson; }

    public Date getTargetDate() { return targetDate; }
    public void setTargetDate(Date targetDate) { this.targetDate = targetDate; }

    public Date getCompletionDate() { return completionDate; }
    public void setCompletionDate(Date completionDate) { this.completionDate = completionDate; }

    public ActionStatus getStatus() { return status; }
    public void setStatus(ActionStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    @Override
    public String toString() {
        return "CAPA #" + actionId + " [" + status + "] - Target: " + targetDate;
    }
}
