package com.industrialinspection.model;

import com.industrialinspection.model.enums.MachineStatus;
import java.sql.Date;
import java.sql.Timestamp;

public class Machine {
    private int machineId;
    private String machineCode;
    private String machineName;
    private String machineType;
    private String productionLine;
    private MachineStatus status;
    private Date installationDate;
    private Date lastMaintenance;
    private Timestamp createdAt;

    public Machine() {
        this.status = MachineStatus.ACTIVE;
    }

    public Machine(int machineId, String machineCode, String machineName, String machineType, 
                   String productionLine, MachineStatus status, Date installationDate) {
        this.machineId = machineId;
        this.machineCode = machineCode;
        this.machineName = machineName;
        this.machineType = machineType;
        this.productionLine = productionLine;
        this.status = status;
        this.installationDate = installationDate;
    }

    public int getMachineId() { return machineId; }
    public void setMachineId(int machineId) { this.machineId = machineId; }

    public String getMachineCode() { return machineCode; }
    public void setMachineCode(String machineCode) { this.machineCode = machineCode; }

    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }

    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }

    public String getProductionLine() { return productionLine; }
    public void setProductionLine(String productionLine) { this.productionLine = productionLine; }

    public MachineStatus getStatus() { return status; }
    public void setStatus(MachineStatus status) { this.status = status; }

    public Date getInstallationDate() { return installationDate; }
    public void setInstallationDate(Date installationDate) { this.installationDate = installationDate; }

    public Date getLastMaintenance() { return lastMaintenance; }
    public void setLastMaintenance(Date lastMaintenance) { this.lastMaintenance = lastMaintenance; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return machineCode + " - " + machineName;
    }
}
