package ru.incomeandexpenses.services;

import ru.incomeandexpenses.dto.SaveOperationDTO;
import ru.incomeandexpenses.models.Operation;

public interface OperationService {
    Operation save(SaveOperationDTO dto);
}
