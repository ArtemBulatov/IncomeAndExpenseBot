package ru.incomeandexpenses.answers;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.incomeandexpenses.IncomeAndExpensesBot;
import ru.incomeandexpenses.dto.AnswerDTO;
import ru.incomeandexpenses.enums.UserStatus;
import ru.incomeandexpenses.models.Client;
import ru.incomeandexpenses.models.Manager;
import ru.incomeandexpenses.services.ButtonsService;
import ru.incomeandexpenses.services.ClientsService;
import ru.incomeandexpenses.services.ManagersService;
import ru.incomeandexpenses.services.MessageService;

import java.util.ArrayList;
import java.util.List;

@Service
public class ManagersAnswerService implements AnswerService{
    private static final String MANAGER_LIST = "Список менеджеров";
    private static final String ADD_NEW_MANAGER = "Добавить нового менеджера";
    private static final String CHOOSE_MANAGER = "Выберите менеджера";
    private static final String ENTER_NEW_MANAGER_NAME = "Введите имя нового менеджера";
    private static final String DELETE_MANAGER = "Удалить менеджера в архив";

    public static final String SHOW_CLIENTS_OF_MANAGER = "Список клиентов этого менеджера";

    private final ManagersService managersService;
    private final ClientsService clientsService;
    private final ButtonsService buttonsService;
    private final MessageService messageService;

    public ManagersAnswerService(ManagersService managersService, ClientsService clientsService, ButtonsService buttonsService, MessageService messageService) {
        this.managersService = managersService;
        this.clientsService = clientsService;
        this.buttonsService = buttonsService;
        this.messageService = messageService;
    }

    @Override
    public AnswerDTO getAnswer(String message, long chatId) {
        AnswerDTO answerDTO = new AnswerDTO();
        List<SendMessage> answers = new ArrayList<>();
        answerDTO.setMessages(answers);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        answers.add(sendMessage);

        if (message.equals("/managers")) {
            String[] managers = new String[] {MANAGER_LIST, ADD_NEW_MANAGER};
            sendMessage.setReplyMarkup(buttonsService.getReplyButtons(managers, true));
            sendMessage.setText("Вы можете посмотреть список менеджеров или добавить нового менеджера");
        }
        else if (message.equals(ADD_NEW_MANAGER)) {
            sendMessage.setText(ENTER_NEW_MANAGER_NAME);
            messageService.addLastAnswer(ENTER_NEW_MANAGER_NAME, chatId);
        }
        else if (messageService.readLastAnswer(chatId).equals(ENTER_NEW_MANAGER_NAME)) {
            Manager manager = managersService.save(message);
            sendMessage.setText("Менеджер сохранён: " + manager.getName());
            messageService.clearAnswers(chatId);
            IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.FREE);
        }
        else if (message.equals(MANAGER_LIST)) {
            String[] managers = managersService.getManagersNames();
            sendMessage.setReplyMarkup(buttonsService.getReplyButtons(managers, true));
            sendMessage.setText(CHOOSE_MANAGER);
            messageService.addLastAnswer(CHOOSE_MANAGER, chatId);
        }
        else if (messageService.readLastAnswer(chatId).equals(CHOOSE_MANAGER)) {
            Manager manager = managersService.get(message);
            String[] buttons = new String[]{SHOW_CLIENTS_OF_MANAGER, DELETE_MANAGER};
            sendMessage.setReplyMarkup(buttonsService.getReplyButtons(buttons, true));
            String managerMessage = "Менеджер: " + manager.getName();
            sendMessage.setText(managerMessage);
            messageService.addLastAnswer(managerMessage, chatId);
        }
        else if (messageService.readLastAnswer(chatId).contains("Менеджер:")) {
            if (message.equals(DELETE_MANAGER)) {
                String managerName = messageService.deleteLastAnswer(chatId).split(":", 2)[1].trim();
                Manager manager = managersService.get(managerName);
                managersService.addManagerInArchive(manager);
                sendMessage.setText("Менеджер " + manager.getName() + " удален в архив");
                messageService.clearAnswers(chatId);
                IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.FREE);
            }
            else if (message.equals(SHOW_CLIENTS_OF_MANAGER)) {
                String managerName = messageService.deleteLastAnswer(chatId).split(":", 2)[1].trim();
                Manager manager = managersService.get(managerName);
                List<Client> clientList = clientsService.getClientsByManagerId(manager.getId());
                if (clientList.isEmpty()) {
                    sendMessage.setText("Не найдено ни одного клиента по менеджеру " + manager.getName());
                    return answerDTO;
                }
                String[] clientsNames = new String[clientList.size()];
                for (int i = 0; i < clientsNames.length; i++) {
                    clientsNames[i] = clientList.get(i).getName();
                }
                sendMessage.setReplyMarkup(buttonsService.getReplyButtons(clientsNames, true));
                sendMessage.setText("Найдены следующие клиенты по менеджеру " + manager.getName());
                messageService.clearAnswers(chatId);
                messageService.addLastAnswer(ClientsAnswerService.CHOOSE_CLIENT, chatId);
                IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.CLIENTS);
            }
        }

        return answerDTO;
    }
}
