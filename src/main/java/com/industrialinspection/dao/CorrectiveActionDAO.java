package com.industrialinspection.dao;

import com.industrialinspection.model.CorrectiveAction;
import java.util.List;

public interface CorrectiveActionDAO {
    void addCorrectiveAction(CorrectiveAction action);
    CorrectiveAction getCorrectiveActionById(int id);
    List<CorrectiveAction> getCorrectiveActionsByInspectionId(int inspectionId);
    List<CorrectiveAction> getAllCorrectiveActions();
    void updateCorrectiveAction(CorrectiveAction action);
    void deleteCorrectiveAction(int id);
}
