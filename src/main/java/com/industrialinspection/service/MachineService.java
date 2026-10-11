package com.industrialinspection.service;

import com.industrialinspection.dao.MachineDAO;
import com.industrialinspection.dao.MachineDAOImpl;
import com.industrialinspection.exception.ValidationException;
import com.industrialinspection.model.Machine;
import com.industrialinspection.model.enums.MachineStatus;
import com.industrialinspection.util.ValidationUtil;

import java.sql.Date;
import java.util.List;

public class MachineService {

    private final MachineDAO machineDAO;

    public MachineService() {
        this(new MachineDAOImpl());
    }

    public MachineService(MachineDAO machineDAO) {
        this.machineDAO = machineDAO;
    }

    public void addMachine(Machine machine, Integer userId) throws ValidationException {
        ValidationUtil.validateNotEmpty(machine.getMachineCode(), "Machine Code");
        ValidationUtil.validateNotEmpty(machine.getMachineName(), "Machine Name");
        ValidationUtil.validateNotEmpty(machine.getMachineType(), "Machine Type");
        ValidationUtil.validateNotEmpty(machine.getProductionLine(), "Production Line");

        if (machine.getInstallationDate() == null) {
            machine.setInstallationDate(new Date(System.currentTimeMillis()));
        }

        machineDAO.addMachine(machine);
        AuditService.getInstance().logAction(userId, "MACHINE_REGISTERED", "machines", machine.getMachineId(),
                "Registered machine: " + machine.getMachineCode() + " (" + machine.getMachineName() + ")");
    }

    public void updateMachine(Machine machine, Integer userId) throws ValidationException {
        ValidationUtil.validateNotEmpty(machine.getMachineName(), "Machine Name");
        machineDAO.updateMachine(machine);
        AuditService.getInstance().logAction(userId, "MACHINE_UPDATED", "machines", machine.getMachineId(),
                "Updated machine: " + machine.getMachineCode());
    }

    public void updateStatus(int machineId, MachineStatus status, Integer userId) {
        Machine m = machineDAO.getMachineById(machineId);
        if (m != null) {
            m.setStatus(status);
            machineDAO.updateMachine(m);
            AuditService.getInstance().logAction(userId, "MACHINE_STATUS_CHANGED", "machines", machineId,
                    "Machine " + m.getMachineCode() + " status changed to " + status);
        }
    }

    public void deleteMachine(int machineId, Integer userId) {
        Machine m = machineDAO.getMachineById(machineId);
        machineDAO.deleteMachine(machineId);
        if (m != null) {
            AuditService.getInstance().logAction(userId, "MACHINE_DELETED", "machines", machineId,
                    "Deleted machine: " + m.getMachineCode());
        }
    }

    public Machine getMachineById(int id) {
        return machineDAO.getMachineById(id);
    }

    public List<Machine> getAllMachines() {
        return machineDAO.getAllMachines();
    }
}
