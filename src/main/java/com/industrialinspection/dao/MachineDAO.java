package com.industrialinspection.dao;

import com.industrialinspection.model.Machine;
import java.util.List;

public interface MachineDAO {
    void addMachine(Machine machine);
    Machine getMachineById(int id);
    Machine getMachineByCode(String code);
    List<Machine> getAllMachines();
    void updateMachine(Machine machine);
    void deleteMachine(int id);
}
