package ru.incomeandexpenses;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.incomeandexpenses.answers.*;
import ru.incomeandexpenses.dto.AnswerDTO;
import ru.incomeandexpenses.enums.UserStatus;
import ru.incomeandexpenses.services.*;
import java.util.*;

@Component
public class IncomeAndExpensesBot extends TelegramLongPollingBot {

    public static final Map<Long, UserStatus> statusMap = new HashMap<>();

    private final FreeAnswerService freeAnswerService;
    private final IncomeAnswerService incomeAnswerService;
    private final ExpenseAnswerService expenseAnswerService;
    private final ClientsAnswerService clientsAnswerService;
    private final AddNewClientAnswerService addNewClientAnswerService;
    private final ManagersAnswerService managersAnswerService;
    private final EncashmentAnswerService encashmentAnswerService;
    private final OperationsAnswerService operationsAnswerService;
    private final ReportAnswerService reportAnswerService;
    private final MessageService messageService;

    @Value("${bot.name}")
    private String botUserName;

    @Value("${bot.token}")
    private String botToken;

    public IncomeAndExpensesBot(
            FreeAnswerService freeAnswerService,
            IncomeAnswerService incomeAnswerService,
            ExpenseAnswerService expenseAnswerService,
            ClientsAnswerService clientsAnswerService,
            AddNewClientAnswerService addNewClientAnswerService,
            ManagersAnswerService managersAnswerService,
            EncashmentAnswerService encashmentAnswerService,
            ReportAnswerService reportAnswerService,
            UsersService usersService,
            OperationsAnswerService operationsAnswerService,
            MessageService messageService
    ) {
        this.freeAnswerService = freeAnswerService;
        this.incomeAnswerService = incomeAnswerService;
        this.expenseAnswerService = expenseAnswerService;
        this.clientsAnswerService = clientsAnswerService;
        this.addNewClientAnswerService = addNewClientAnswerService;
        this.managersAnswerService = managersAnswerService;
        this.encashmentAnswerService = encashmentAnswerService;
        this.reportAnswerService = reportAnswerService;
        this.operationsAnswerService = operationsAnswerService;
        this.messageService = messageService;
        usersService.getAll().forEach(user -> statusMap.put(user.getChatId(), UserStatus.FREE));
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        long messageChatId = update.getMessage().getChatId();
        answerToMessage(update, messageChatId);
    }

    private void answerToMessage(Update update, long messageChatId) {
        String messageText = update.getMessage().getText().trim();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(messageChatId));

//        if (messageText.equals("/start")) {
//            String userName = update.getMessage().getChat().getFirstName();
//            User user = usersService.save(messageChatId, userName);
//            fillUsersMap();
//            addStacksToUsers();
//            sendMessage.setText("Пользователь " + user.getName() + " сохранён");
//            sendTheMessage(sendMessage);
//            return;
//        }
        if (messageChatId == 711842183) {
            sendMessage.setText("Я работаю, всё ок");
            sendAnswerMessage(sendMessage);
            return;
        }
        if (!statusMap.containsKey(messageChatId)) {
            return;
        }

        switch (messageText) {
            case "/income" -> statusMap.put(messageChatId, UserStatus.INCOME);
            case "/expense" -> statusMap.put(messageChatId, UserStatus.EXPENSE);
            case "/clients" -> statusMap.put(messageChatId, UserStatus.CLIENTS);
            case "/add_new_client" -> statusMap.put(messageChatId, UserStatus.ADD_NEW_CLIENT);
            case "/managers" -> statusMap.put(messageChatId, UserStatus.MANAGERS);
            case "/encashment" -> statusMap.put(messageChatId, UserStatus.ENCASHMENT);
            case "/operations" -> statusMap.put(messageChatId, UserStatus.OPERATIONS);
            case "/reports" -> statusMap.put(messageChatId, UserStatus.REPORTS);
        };

        UserStatus status = statusMap.get(messageChatId);

        AnswerService answerService = switch (status) {
            case INCOME -> incomeAnswerService;
            case EXPENSE -> expenseAnswerService;
            case CLIENTS -> clientsAnswerService;
            case ADD_NEW_CLIENT -> addNewClientAnswerService;
            case MANAGERS -> managersAnswerService;
            case ENCASHMENT -> encashmentAnswerService;
            case OPERATIONS -> operationsAnswerService;
            case REPORTS -> reportAnswerService;
            default -> {
                statusMap.put(messageChatId, UserStatus.FREE);
                yield freeAnswerService;
            }
        };

        AnswerDTO answerDTO = answerService.getAnswer(messageText, messageChatId);
        if (!answerDTO.getMessages().isEmpty()) {
            answerDTO.getMessages().forEach(this::sendAnswerMessage);
        }
        if (answerDTO.getDocument() != null) {
            sendAnswerDocument(answerDTO.getDocument());
        }

        messageService.addLastMessage(messageText, messageChatId);
    }

    private void sendAnswerMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendAnswerDocument(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}