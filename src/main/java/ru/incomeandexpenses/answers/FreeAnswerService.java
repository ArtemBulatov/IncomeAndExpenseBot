package ru.incomeandexpenses.answers;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.incomeandexpenses.dto.AnswerDTO;

import java.util.ArrayList;
import java.util.List;

@Service
public class FreeAnswerService implements AnswerService{
    @Override
    public AnswerDTO getAnswer(String message, long chatId) {
        AnswerDTO answerDTO = new AnswerDTO();
        List<SendMessage> answers = new ArrayList<>();
        answerDTO.setMessages(answers);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Извините, я вас не понимаю. Попробуйте воспользоваться меню");
        answers.add(sendMessage);
        return answerDTO;
    }
}
