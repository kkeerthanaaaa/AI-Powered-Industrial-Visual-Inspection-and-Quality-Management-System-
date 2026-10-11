package com.industrialinspection.service;

import com.industrialinspection.dao.OperatorDAO;
import com.industrialinspection.dao.OperatorDAOImpl;
import com.industrialinspection.exception.ValidationException;
import com.industrialinspection.model.Operator;
import com.industrialinspection.model.enums.OperatorStatus;
import com.industrialinspection.util.ValidationUtil;

import java.util.List;

public class OperatorService {

    private final OperatorDAO operatorDAO;

    public OperatorService() {
        this(new OperatorDAOImpl());
    }

    public OperatorService(OperatorDAO operatorDAO) {
        this.operatorDAO = operatorDAO;
    }

    public void addOperator(Operator operator, Integer userId) throws ValidationException {
        ValidationUtil.validateNotEmpty(operator.getName(), "Operator Name");
        ValidationUtil.validateNotEmpty(operator.getEmployeeCode(), "Employee Code");
        ValidationUtil.validateNotEmpty(operator.getDepartment(), "Department");
        ValidationUtil.validateNotEmpty(operator.getContact(), "Contact Info");

        operatorDAO.addOperator(operator);
        AuditService.getInstance().logAction(userId, "OPERATOR_REGISTERED", "operators", operator.getOperatorId(),
                "Registered operator: " + operator.getName() + " (" + operator.getEmployeeCode() + ")");
    }

    public void updateOperator(Operator operator, Integer userId) throws ValidationException {
        ValidationUtil.validateNotEmpty(operator.getName(), "Operator Name");
        operatorDAO.updateOperator(operator);
        AuditService.getInstance().logAction(userId, "OPERATOR_UPDATED", "operators", operator.getOperatorId(),
                "Updated operator: " + operator.getName());
    }

    public void updateStatus(int operatorId, OperatorStatus status, Integer userId) {
        Operator o = operatorDAO.getOperatorById(operatorId);
        if (o != null) {
            o.setStatus(status);
            operatorDAO.updateOperator(o);
            AuditService.getInstance().logAction(userId, "OPERATOR_STATUS_CHANGED", "operators", operatorId,
                    "Operator " + o.getName() + " status changed to " + status);
        }
    }

    public void deleteOperator(int operatorId, Integer userId) {
        Operator o = operatorDAO.getOperatorById(operatorId);
        operatorDAO.deleteOperator(operatorId);
        if (o != null) {
            AuditService.getInstance().logAction(userId, "OPERATOR_DELETED", "operators", operatorId,
                    "Deleted operator: " + o.getName());
        }
    }

    public Operator getOperatorById(int id) {
        return operatorDAO.getOperatorById(id);
    }

    public List<Operator> getAllOperators() {
        return operatorDAO.getAllOperators();
    }
}
