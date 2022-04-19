package ru.incomeandexpenses.answers;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.incomeandexpenses.IncomeAndExpensesBot;
import ru.incomeandexpenses.dto.AnswerDTO;
import ru.incomeandexpenses.dto.SaveOperationDTO;
import ru.incomeandexpenses.enums.UserStatus;
import ru.incomeandexpenses.services.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseAnswerService implements AnswerService{
    private static final String ENTER_EXPENSE_VALUE = "Введите сумму расхода";
    private static final String ENTER_PURPOSE = "Введите назначение расхода";
    private final Map<Long, SaveOperationDTO> saveDataMap;
    private final UsersService usersService;
    private final MessageService messageService;
    private final ClientsService clientsService;
    private final ExpensesService expensesService;

    public ExpenseAnswerService(UsersService usersService, MessageService messageService, ClientsService clientsService, ExpensesService expensesService) {
        this.usersService = usersService;
        this.messageService = messageService;
        this.clientsService = clientsService;
        this.expensesService = expensesService;
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
        if (message.equals("/expense")) {
            SaveOperationDTO expense = new SaveOperationDTO();
            expense.setAuthor(usersService.getUser(chatId));
            String clientName = ClientsService.OTHERS;
            if (messageService.getSizeOfAnswers(chatId) > 0
                    && messageService.readLastAnswer(chatId).contains("Клиент:")) {
                clientName = messageService.deleteLastAnswer(chatId).split(":", 2)[1].trim();
            }
            expense.setClient(clientsService.getClientByName(clientName));
            saveDataMap.put(chatId, expense);
            sendMessage.setText(ENTER_EXPENSE_VALUE);
            messageService.addLastAnswer(ENTER_EXPENSE_VALUE, chatId);
        }
        else if (messageService.readLastAnswer(chatId).equals(ENTER_EXPENSE_VALUE)) {
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
            expensesService.save(dto);
            sendMessage.setText("Сохранён расход " + dto.getValue() + " руб. Назначение: " + dto.getPurpose());
            String messageToAnotherUsers = dto.getAuthor().getName() + " сохранил расход "
                    + dto.getValue() + " руб. Назначение: " + dto.getPurpose();
            answers.addAll(messageService.getMessagesToAnotherUsers(messageToAnotherUsers, chatId));
            messageService.clearAnswers(chatId);
            IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.FREE);
        }
        return answerDTO;
    }
}
