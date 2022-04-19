package ru.incomeandexpenses.dto;

import lombok.Data;
import ru.incomeandexpenses.models.Client;
import ru.incomeandexpenses.models.User;

@Data
public class SaveOperationDTO {
    private int value;
    private User author;
    private Client client;
    private String purpose;
}
