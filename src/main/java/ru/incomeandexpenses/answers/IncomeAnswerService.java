package ru.incomeandexpenses.answers;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.incomeandexpenses.IncomeAndExpensesBot;
import ru.incomeandexpenses.dto.AnswerDTO;
import ru.incomeandexpenses.dto.SaveOperationDTO;
import ru.incomeandexpenses.enums.UserStatus;
import ru.incomeandexpenses.services.ClientsService;
import ru.incomeandexpenses.services.IncomeService;
import ru.incomeandexpenses.services.MessageService;
import ru.incomeandexpenses.services.UsersService;

import java.util.*;

@Service
public class IncomeAnswerService implements AnswerService{
    private static final String ENTER_INCOME_VALUE = "Введите сумму дохода";
    private static final String ENTER_PURPOSE = "Введите назначение дохода";
    private final Map<Long, SaveOperationDTO> saveDataMap;
    private final UsersService usersService;
    private final MessageService messageService;
    private final ClientsService clientsService;
    private final IncomeService incomeService;

    public IncomeAnswerService(UsersService usersService, MessageService messageService, ClientsService clientsService, IncomeService incomeService) {
        this.usersService = usersService;
        this.messageService = messageService;
        this.clientsService = clientsService;
        this.incomeService = incomeService;
        this.saveDataMap = new HashMap<>();
    }

    @Override
    public AnswerDTO getAnswer(String message, long chatId) {
        AnswerDTO answerDTO = new AnswerDTO();
        List<SendMessage> answers = new ArrayList<>();
        answerDTO.setMessages(answers);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        answers.add(sendMessage);
        if (message.equals("/income")) {
            SaveOperationDTO income = new SaveOperationDTO();
            income.setAuthor(usersService.getUser(chatId));
            String clientName = ClientsService.OTHERS;
            if (messageService.getSizeOfAnswers(chatId) > 0
                    && messageService.readLastAnswer(chatId).contains("Клиент:")) {
                clientName = messageService.deleteLastAnswer(chatId).split(":", 2)[1].trim();
            }
            income.setClient(clientsService.getClientByName(clientName));
            saveDataMap.put(chatId, income);
            sendMessage.setText(ENTER_INCOME_VALUE);
            messageService.addLastAnswer(ENTER_INCOME_VALUE, chatId);
        }
        else if (messageService.readLastAnswer(chatId).equals(ENTER_INCOME_VALUE)) {
            if(message.matches("\\d+")) {
                saveDataMap.get(chatId).setValue(Integer.parseInt(message));
                sendMessage.setText(ENTER_PURPOSE);
                messageService.addLastAnswer(ENTER_PURPOSE, chatId);
            }
            else {
                sendMessage.setText("Введите только цифры");
            }
        }
        else if (messageService.readLastAnswer(chatId).equals(ENTER_PURPOSE)) {
            saveDataMap.get(chatId).setPurpose(message);
            SaveOperationDTO dto = saveDataMap.get(chatId);
            incomeService.save(dto);
            String clientName = "";
            if (!dto.getClient().getName().equals(ClientsService.OTHERS)){
                clientName = " по клиенту " + dto.getClient().getName();
            }
            sendMessage.setText("Сохранён доход " + dto.getValue() + " руб." + clientName
                    + "\nНазначение: " + dto.getPurpose());
            String messageToAnotherUsers = dto.getAuthor().getName() + " сохранил доход "
                    + dto.getValue() + " руб." + clientName + "\nНазначение: " + dto.getPurpose();
            answers.addAll(messageService.getMessagesToAnotherUsers(messageToAnotherUsers, chatId));
            messageService.clearAnswers(chatId);
            IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.FREE);
        }
        return answerDTO;
    }

}
