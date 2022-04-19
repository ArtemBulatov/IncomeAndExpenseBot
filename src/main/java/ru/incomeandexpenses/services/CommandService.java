package ru.incomeandexpenses.services;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.incomeandexpenses.models.Client;

import java.util.List;

@Service
public class CommandService {

    public static final String INCOME_COMMAND = "/income";
    public static final String EXPENSE_COMMAND = "/expense";
    public static final String ENCASHMENT_GET = "Получил";
    public static final String ENCASHMENT_GIVE = "Отдал";

    private final MessageService messageService;
    private final ButtonsService buttonsService;
    private final ClientsService clientsService;
    private final ManagersService managersService;

    public CommandService(MessageService messageService, ButtonsService buttonsService, ClientsService clientsService, ManagersService managersService) {
        this.messageService = messageService;
        this.buttonsService = buttonsService;
        this.clientsService = clientsService;
        this.managersService = managersService;
    }

    public SendMessage getAnswerToCommand(String command, long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        switch (command) {
            case "/clients" -> {
                List<Client> clientList = clientsService.getClientListToButtons();
                if (clientList.isEmpty()) {
                    sendMessage.setText("Не найдено ни одного клиента. Создайте клиентов");
                    return sendMessage;
                }
                String[] clientsNames = new String[clientList.size()];
                for (int i = 0; i < clientsNames.length; i++) {
                    clientsNames[i] = clientList.get(i).getName();
                }
                sendMessage.setReplyMarkup(buttonsService.getReplyButtons(clientsNames, true));
                sendMessage.setText("Выберите клиента");
                return sendMessage;
            }
            case "/managers" -> {
                String[] managers = managersService.getManagersNames();
                sendMessage.setReplyMarkup(buttonsService.getReplyButtons(managers, true));
                sendMessage.setText("Выберите менеджера");
                return sendMessage;
            }
            case "/add_new_manager" -> {
                sendMessage.setText("Введите имя менеджера");
                return sendMessage;
            }
            case "/encashment" -> {
                String[] buttons = new String[] {ENCASHMENT_GET, ENCASHMENT_GIVE};
                sendMessage.setReplyMarkup(buttonsService.getReplyButtons(buttons, true));
                sendMessage.setText("Выберите действие");
                return sendMessage;
            }

            case "/reports" -> {
                String[] reports = new String[] {"Получить файл", "Закрыть сверку"};
                sendMessage.setText("Выберите отчёт");
                sendMessage.setReplyMarkup(buttonsService.getReplyButtons(reports, true));
                return sendMessage;
            }
        }
        return sendMessage;
    }
}
