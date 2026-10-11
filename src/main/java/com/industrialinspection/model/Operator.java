package com.industrialinspection.model;

import com.industrialinspection.model.enums.OperatorStatus;
import com.industrialinspection.model.enums.Shift;
import java.sql.Timestamp;

public class Operator {
    private int operatorId;
    private String name;
    private String employeeCode;
    private String department;
    private Shift shift;
    private String contact;
    private OperatorStatus status;
    private Timestamp createdAt;

    public Operator() {
        this.shift = Shift.MORNING;
        this.status = OperatorStatus.ACTIVE;
    }

    public Operator(int operatorId, String name, String employeeCode, String department, 
                    Shift shift, String contact, OperatorStatus status) {
        this.operatorId = operatorId;
        this.name = name;
        this.employeeCode = employeeCode;
        this.department = department;
        this.shift = shift;
        this.contact = contact;
        this.status = status;
    }

    public int getOperatorId() { return operatorId; }
    public void setOperatorId(int operatorId) { this.operatorId = operatorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Shift getShift() { return shift; }
    public void setShift(Shift shift) { this.shift = shift; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public OperatorStatus getStatus() { return status; }
    public void setStatus(OperatorStatus status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + employeeCode + ") - Shift: " + shift;
    }
}
