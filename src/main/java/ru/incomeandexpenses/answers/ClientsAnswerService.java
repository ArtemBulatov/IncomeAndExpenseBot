package ru.incomeandexpenses.answers;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.incomeandexpenses.IncomeAndExpensesBot;
import ru.incomeandexpenses.dto.AnswerDTO;
import ru.incomeandexpenses.enums.UserStatus;
import ru.incomeandexpenses.models.Client;
import ru.incomeandexpenses.models.Manager;
import ru.incomeandexpenses.models.Salary;
import ru.incomeandexpenses.models.User;
import ru.incomeandexpenses.services.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClientsAnswerService implements AnswerService{
    public static final String CHOOSE_CLIENT = "Выберите клиента";
    private static final String INCOME_TEXT = "Доход";
    private static final String EXPENSE_TEXT = "Расход";
    private static final String SALARY_TEXT = "Зарплата";
    private static final String EDIT_CLIENT = "Изменить данные клиента";
    private static final String ENTER_SALARY_VALUE = "Введите сумму";
    private static final String ENTER_PURPOSE = "Введите назначение суммы";
    private static final String CHANGE_MANAGER_OF_CLIENT = "Изменить менеджера";
    private static final String DELETE_CLIENT = "Удалить клиента в архив";

    private final UsersService usersService;
    private final ClientsService clientsService;
    private final ManagersService managersService;
    private final ButtonsService buttonsService;
    private final MessageService messageService;
    private final IncomeAnswerService incomeAnswerService;
    private final ExpenseAnswerService expenseAnswerService;
    private final SalariesService salariesService;
    private final AddNewClientAnswerService addNewClientAnswerService;

    public ClientsAnswerService(UsersService usersService, ClientsService clientsService, ManagersService managersService, ButtonsService buttonsService, MessageService messageService, IncomeAnswerService incomeAnswerService, ExpenseAnswerService expenseAnswerService, SalariesService salariesService, AddNewClientAnswerService addNewClientAnswerService) {
        this.usersService = usersService;
        this.clientsService = clientsService;
        this.managersService = managersService;
        this.buttonsService = buttonsService;
        this.messageService = messageService;
        this.incomeAnswerService = incomeAnswerService;
        this.expenseAnswerService = expenseAnswerService;
        this.salariesService = salariesService;
        this.addNewClientAnswerService = addNewClientAnswerService;
    }

    @Override
    public AnswerDTO getAnswer(String message, long chatId) {
        AnswerDTO answerDTO = new AnswerDTO();
        List<SendMessage> answers = new ArrayList<>();
        answerDTO.setMessages(answers);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        answers.add(sendMessage);

        if (message.equals("/clients")) {
            List<Client> clientList = clientsService.getClientListToButtons();
            if (clientList.isEmpty()) {
                sendMessage.setText("Не найдено ни одного клиента. Создайте клиентов");
                messageService.clearAnswers(chatId);
                IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.FREE);
                return answerDTO;
            }
            String[] clientsNames = new String[clientList.size()];
            for (int i = 0; i < clientsNames.length; i++) {
                clientsNames[i] = clientList.get(i).getName();
            }
            sendMessage.setReplyMarkup(buttonsService.getReplyButtons(clientsNames, true));
            sendMessage.setText(CHOOSE_CLIENT);
            messageService.addLastAnswer(CHOOSE_CLIENT, chatId);
        }
        else if (messageService.readLastAnswer(chatId).equals(CHOOSE_CLIENT)) {
            Client client = clientsService.getClientByName(message);
            String[] buttons = new String[4];
            buttons[0] = INCOME_TEXT;
            buttons[1] = EXPENSE_TEXT;
            buttons[2] = SALARY_TEXT;
            buttons[3] = EDIT_CLIENT;
            sendMessage.setReplyMarkup(buttonsService.getReplyButtons(buttons, true));
            sendMessage.setText("Клиент: " + client.getName());
            messageService.addLastAnswer("Клиент: " + client.getName(), chatId);
        }
        else if (messageService.readLastAnswer(chatId).contains(("Клиент:"))) {
            switch (message) {
                case INCOME_TEXT -> {
                    IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.INCOME);
                    answerDTO = incomeAnswerService.getAnswer("/income", chatId);
                }
                case EXPENSE_TEXT -> {
                    IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.EXPENSE);
                    answerDTO = expenseAnswerService.getAnswer("/expense", chatId);
                }
                case SALARY_TEXT -> {
                    sendMessage.setText(ENTER_SALARY_VALUE);
                    messageService.addLastAnswer(ENTER_SALARY_VALUE, chatId);
                }
                case EDIT_CLIENT -> {
                    String clientName = messageService.readLastAnswer(chatId).split(":", 2)[1].trim();
                    Client client = clientsService.getClientByName(clientName);
                    Manager manager = managersService.get(client.getManagerId());

                    String[] buttons = new String[]{CHANGE_MANAGER_OF_CLIENT, DELETE_CLIENT};
                    sendMessage.setReplyMarkup(buttonsService.getReplyButtons(buttons, true));
                    sendMessage.setText(messageService.readLastAnswer(chatId)
                            + "\n Его менеджер: " + manager.getName()
                            + "\nВы можете изменить менеджера клиента или удалить клиента в архив");
                }
            }
        }
        else if (messageService.readLastAnswer(chatId).equals(ENTER_SALARY_VALUE)) {
            if (message.matches("\\d+")) {
                sendMessage.setText(ENTER_PURPOSE);
                messageService.addLastAnswer(ENTER_PURPOSE, chatId);
            }
            else {
                sendMessage.setText("Введите только цифры");
            }
        }
        else if (messageService.getSizeOfAnswers(chatId) > 3
                && messageService.readLastMessage(chatId).matches("\\d+")
                && messageService.deleteLastAnswer(chatId).equals(ENTER_PURPOSE)
                && messageService.deleteLastAnswer(chatId).equals(ENTER_SALARY_VALUE)) {
            String purpose = message;
            String clientName = messageService.readLastAnswer(chatId).split(":", 2)[1].trim();
            Client client = clientsService.getClientByName(clientName);
            User user = usersService.getUser(chatId);
            int salaryValue = Integer.parseInt(messageService.readLastMessage(chatId));
            Salary salary = salariesService.save(salaryValue, user, client, purpose);

            sendMessage.setText("Сохранена зарплата " + salary.getValue() + " руб. по клиенту " + client.getName()
                    + ". Назначение: " + purpose);
            String notify = user.getName() + " сохранил зарплату "
                    + salary.getValue() + " руб. по клиенту " + client.getName() + ". Назначение: " + purpose;
            answers.addAll(messageService.getMessagesToAnotherUsers(notify, chatId));

            messageService.clearMessages(chatId);
            messageService.clearAnswers(chatId);
            IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.FREE);
        }


        if (message.equals(CHANGE_MANAGER_OF_CLIENT)) {
            String clientName = messageService.readLastAnswer(chatId).split(":", 2)[1].trim();
            Client client = clientsService.getClientByName(clientName);
            messageService.addLastAnswer(AddNewClientAnswerService.ENTER_CLIENT_NAME, chatId);
            IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.ADD_NEW_CLIENT);
            answerDTO = addNewClientAnswerService.getAnswer(client.getName(), chatId);
        }
        if (message.equals(DELETE_CLIENT)) {
            String clientName = messageService.readLastAnswer(chatId).split(":", 2)[1].trim();
            Client client = clientsService.getClientByName(clientName);
            clientsService.addClientInArchive(client);
            sendMessage.setText("Клиент " + clientName + " удален в архив");

            messageService.clearMessages(chatId);
            messageService.clearAnswers(chatId);
            IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.FREE);
        }

        return answerDTO;
    }
}
