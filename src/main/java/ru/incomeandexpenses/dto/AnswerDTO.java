package ru.incomeandexpenses.dto;

import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.List;

@Data
public class AnswerDTO {
    private List<SendMessage> messages = new ArrayList<>();
    private SendDocument document;
}
