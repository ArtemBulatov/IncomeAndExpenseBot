package ru.incomeandexpenses.answers;

import org.springframework.context.annotation.Lazy;
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
public class AddNewClientAnswerService implements AnswerService{
    public static final String ENTER_CLIENT_NAME = "Введите имя клиента";
    private static final String CHOOSE_MANAGER = "Выберите менеджера для клиента:";

    private final ClientsService clientsService;
    private final MessageService messageService;
    private final ManagersService managersService;
    private final ButtonsService buttonsService;
    private final ClientsAnswerService clientsAnswerService;

    public AddNewClientAnswerService(ClientsService clientsService, MessageService messageService, ManagersService managersService, ButtonsService buttonsService, @Lazy ClientsAnswerService clientsAnswerService) {
        this.clientsService = clientsService;
        this.messageService = messageService;
        this.managersService = managersService;
        this.buttonsService = buttonsService;
        this.clientsAnswerService = clientsAnswerService;
    }

    @Override
    public AnswerDTO getAnswer(String message, long chatId) {
        AnswerDTO answerDTO = new AnswerDTO();
        List<SendMessage> answers = new ArrayList<>();
        answerDTO.setMessages(answers);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        answers.add(sendMessage);
        if (message.equals("/add_new_client")) {
            sendMessage.setText(ENTER_CLIENT_NAME);
            messageService.addLastAnswer(ENTER_CLIENT_NAME, chatId);
        }
        else if(messageService.readLastAnswer(chatId).equals(ENTER_CLIENT_NAME)) {
            String answer = CHOOSE_MANAGER + " " + message;
            sendMessage.setText(answer);
            messageService.addLastAnswer(answer, chatId);
            String[] managerNames = managersService.getManagersNames();
            sendMessage.setReplyMarkup(buttonsService.getReplyButtons(managerNames, true));
        }
        else if(messageService.readLastAnswer(chatId).contains(CHOOSE_MANAGER)) {
            Manager manager = managersService.get(message);
            String clientName = messageService.deleteLastAnswer(chatId).split(":",2)[1].trim();
            Client client = clientsService.save(clientName, manager.getId());
            sendMessage.setText("Клиент сохранён: " + client.getName() + ". \nЕго менеджер: " + manager.getName());

            messageService.clearAnswers(chatId);
            messageService.addLastAnswer(ClientsAnswerService.CHOOSE_CLIENT, chatId);

            answers.addAll(clientsAnswerService.getAnswer(client.getName(), chatId).getMessages());
            IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.CLIENTS);
        }
        return answerDTO;
    }
}
