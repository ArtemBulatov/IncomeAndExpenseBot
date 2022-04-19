package ru.incomeandexpenses.answers;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.incomeandexpenses.IncomeAndExpensesBot;
import ru.incomeandexpenses.dto.AnswerDTO;
import ru.incomeandexpenses.dto.SaveOperationDTO;
import ru.incomeandexpenses.enums.UserStatus;
import ru.incomeandexpenses.services.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class EncashmentAnswerService implements AnswerService{
    private static final String ENCASHMENT_GET = "Получил";
    private static final String ENCASHMENT_GIVE = "Отдал";
    private static final String CHOOSE_ACTION = "Выберите действие";

    private final ButtonsService buttonsService;
    private final MessageService messageService;
    private final UsersService usersService;
    private final ClientsService clientsService;
    private final IncomeService incomeService;
    private final ExpensesService expensesService;

    public EncashmentAnswerService(ButtonsService buttonsService, MessageService messageService, UsersService usersService, ClientsService clientsService, IncomeService incomeService, ExpensesService expensesService) {
        this.buttonsService = buttonsService;
        this.messageService = messageService;
        this.usersService = usersService;
        this.clientsService = clientsService;
        this.incomeService = incomeService;
        this.expensesService = expensesService;
    }

    @Override
    public AnswerDTO getAnswer(String message, long chatId) {
        AnswerDTO answerDTO = new AnswerDTO();
        List<SendMessage> answers = new ArrayList<>();
        answerDTO.setMessages(answers);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        answers.add(sendMessage);
        if (message.equals("/encashment")) {
            String[] buttons = new String[] {ENCASHMENT_GET, ENCASHMENT_GIVE};
            sendMessage.setReplyMarkup(buttonsService.getReplyButtons(buttons, true));
            sendMessage.setText(CHOOSE_ACTION);
            messageService.addLastAnswer(CHOOSE_ACTION, chatId);
        }
        else if (messageService.readLastAnswer(chatId).equals(CHOOSE_ACTION)
                && (message.equals(ENCASHMENT_GET) || message.equals(ENCASHMENT_GIVE))) {
            sendMessage.setText("Введите сумму");
            messageService.addLastAnswer(message, chatId);
        }
        else if (messageService.readLastAnswer(chatId).equals(ENCASHMENT_GET)) {
            if (message.matches("\\d+")) {
                int value = Integer.parseInt(message);
                SaveOperationDTO income = new SaveOperationDTO();
                income.setAuthor(usersService.getUser(chatId));
                income.setClient(clientsService.getClientByName(ClientsService.ENCASHMENT));
                income.setValue(value);
                income.setPurpose(ClientsService.ENCASHMENT);
                incomeService.save(income);
                sendMessage.setText("+" + income.getValue() + " руб. Назначение: " + ClientsService.ENCASHMENT);

                income.setAuthor(usersService.getAll().stream().filter(user -> user.getChatId() != chatId).findAny().get());
                String notify = "-" + income.getValue() + " руб. Назначение: " + ClientsService.ENCASHMENT;
                answers.addAll(messageService.getMessagesToAnotherUsers(notify, chatId));
                expensesService.save(income);

                messageService.clearAnswers(chatId);
                IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.FREE);
            }
            else {
                sendMessage.setText("Введите только цифры");
            }
        }
        else if (messageService.readLastAnswer(chatId).equals(ENCASHMENT_GIVE)) {
            if (message.matches("\\d+")) {
                int value = Integer.parseInt(message);
                SaveOperationDTO expense = new SaveOperationDTO();
                expense.setAuthor(usersService.getUser(chatId));
                expense.setClient(clientsService.getClientByName(ClientsService.ENCASHMENT));
                expense.setValue(value);
                expense.setPurpose(ClientsService.ENCASHMENT);
                expensesService.save(expense);
                sendMessage.setText("-" + expense.getValue() + " руб. Назначение: " + ClientsService.ENCASHMENT);

                expense.setAuthor(usersService.getAll().stream().filter(user -> user.getChatId() != chatId).findAny().get());
                String notify = "+" + expense.getValue() + " руб. Назначение: " + ClientsService.ENCASHMENT;
                answers.addAll(messageService.getMessagesToAnotherUsers(notify, chatId));
                incomeService.save(expense);

                messageService.clearAnswers(chatId);
                IncomeAndExpensesBot.statusMap.put(chatId, UserStatus.FREE);
            }
            else {
                sendMessage.setText("Введите только цифры");
            }
        }
        return answerDTO;
    }
}
