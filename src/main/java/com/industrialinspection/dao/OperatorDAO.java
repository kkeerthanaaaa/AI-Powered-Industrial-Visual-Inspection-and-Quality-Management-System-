package com.industrialinspection.dao;

import com.industrialinspection.model.Operator;
import java.util.List;

public interface OperatorDAO {
    void addOperator(Operator operator);
    Operator getOperatorById(int id);
    Operator getOperatorByCode(String code);
    List<Operator> getAllOperators();
    void updateOperator(Operator operator);
    void deleteOperator(int id);
}
