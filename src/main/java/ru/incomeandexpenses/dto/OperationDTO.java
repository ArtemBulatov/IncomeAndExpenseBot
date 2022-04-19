package ru.incomeandexpenses.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OperationDTO {
    private UUID operationId;
    private String operationName;
    private int value;
    private String author;
    private String client;
    private String stringDate;
    private LocalDateTime date;
    private String purpose;
}
