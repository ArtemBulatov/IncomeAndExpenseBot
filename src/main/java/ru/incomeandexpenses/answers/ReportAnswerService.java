package ru.incomeandexpenses.answers;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.incomeandexpenses.IncomeAndExpensesBot;
import ru.incomeandexpenses.dto.AnswerDTO;
import ru.incomeandexpenses.enums.UserStatus;
import ru.incomeandexpenses.models.Verify;
import ru.incomeandexpenses.services.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportAnswerService implements AnswerService{
    private final ButtonsService buttonsService;
    private final FilesService filesService;
    private final VerifyService verifyService;
    private final MessageService messageService;
    private final UsersService usersService;

    public ReportAnswerService(ButtonsService buttonsService, FilesService filesService, VerifyService verifyService, MessageService messageService, UsersService usersService) {
        this.buttonsService = buttonsService;
        this.filesService = filesService;
        this.verifyService = verifyService;
        this.messageService = messageService;
        this.usersService = usersService;
    }

    @Override
    public AnswerDTO getAnswer(String message, long chatId) {
        AnswerDTO answerDTO = new AnswerDTO();
        List<SendMessage> answers = new ArrayList<>();
        answerDTO.setMessages(answers);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        answers.add(sendMessage);

        if(message.equals("/reports")) {
            String[] reports = new String[] {"Получить файл", "Закрыть сверку"};
            sendMessage.setText("Выберите отчёт");
            sendMessage.setReplyMarkup(buttonsService.getReplyButtons(reports, true));
        }
        if (message.equals("Получить файл")) {
            InputFile inputFile = filesService.createReport();
            if (inputFile != null) {
                SendDocument sendDocument = new SendDocument();
                sendDocument.setChatId(String.valueOf(chatId));
                sendDocument.setDocument(inputFile);
                answerDTO.setDocument(sendDocument);
                sendMessage.setText("Отчёт готов");
            }
            else {
                sendMessage.setText("Произошла ошибка при создании файла");
            }
        }
        if (message.equals("Закрыть сверку")) {
            sendMessage.setText("Вы действительно хотите закрыть сверку?");
            String[] buttons = new String[] {"Да, закрыть сверку", "Нет, я ошибся"};
            sendMessage.setReplyMarkup(buttonsService.getReplyButtons(buttons, true));
        }
        if (message.equals("Да, закрыть сверку") && messageService.readLastMessage(chatId).equals("Закрыть сверку")) {
            InputFile inputFile = filesService.createReport();
            if (inputFile != null) {
                Verify verify = verifyService.createNewVerify();
                String verifyDate = verify.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                sendMessage.setText( "Сверка закрыта: " +verifyDate);

                SendDocument sendDocument = new SendDocument();
                sendDocument.setChatId(String.valueOf(chatId));
                sendDocument.setDocument(inputFile);
                answerDTO.setDocument(sendDocument);

                messageService.clearMessages(chatId);
                String notify = usersService.getUser(chatId).getName() + " закрыл сверку: " + verifyDate;
                answers.addAll(messageService.getMessagesToAnotherUsers(notify, chatId));
                IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.FREE);
            }
            else {
                sendMessage.setText("Произошла ошибка при создании файла");
            }
        }
        if (message.equals("Нет, я ошибся") && messageService.readLastMessage(chatId).equals("Закрыть сверку")) {
            sendMessage.setText("Ничего, бывает");
        }
        return answerDTO;
    }
}
