package ru.incomeandexpenses.answers;

import ru.incomeandexpenses.dto.AnswerDTO;

public interface AnswerService {
    AnswerDTO getAnswer(String message, long chatId);
}
