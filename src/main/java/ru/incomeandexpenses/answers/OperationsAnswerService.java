package ru.incomeandexpenses.answers;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.incomeandexpenses.dto.AnswerDTO;
import ru.incomeandexpenses.dto.OperationDTO;
import ru.incomeandexpenses.models.User;
import ru.incomeandexpenses.services.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class OperationsAnswerService implements AnswerService{
    private static final String YES = "Да";
    private static final String NO = "Нет";

    private final DtoService dtoService;
    private final UsersService usersService;
    private final ButtonsService buttonsService;
    private final MessageService messageService;
    private final IncomeService incomeService;
    private final ExpensesService expensesService;
    private final Map<Long, List<String>> operationsStringBuffer;
    private final Map<Long, List<OperationDTO>> operationsDtoBuffer;

    public OperationsAnswerService(DtoService dtoService, UsersService usersService, ButtonsService buttonsService, MessageService messageService, IncomeService incomeService, ExpensesService expensesService) {
        this.dtoService = dtoService;
        this.usersService = usersService;
        this.buttonsService = buttonsService;
        this.messageService = messageService;
        this.incomeService = incomeService;
        this.expensesService = expensesService;
        this.operationsStringBuffer = new HashMap<>();
        this.operationsDtoBuffer = new HashMap<>();
    }

    @Override
    public AnswerDTO getAnswer(String message, long chatId) {
        AnswerDTO answerDTO = new AnswerDTO();
        List<SendMessage> answers = new ArrayList<>();
        answerDTO.setMessages(answers);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        answers.add(sendMessage);
        if (message.equals("/operations")) {
            LocalDateTime date = LocalDateTime.now(ZoneId.of("Europe/Moscow")).minusDays(7);
            User user = usersService.getUser(chatId);
            List<OperationDTO> operations = dtoService.getOperationDtoListForUser(date, user);

            if (operations.size() == 0) {
                sendMessage.setText("За последние 7 дней Вы не сделали ни одной операции");
                return answerDTO;
            }

            List<String> operationStrings = new ArrayList<>();
            for (int i = 0; i < operations.size(); i++) {
                operationStrings.add(i, getOperationString(operations.get(i)));
            }
            sendMessage.setText("Ваши операции за последние 7 дней");
            sendMessage.setReplyMarkup(buttonsService.getReplyButtons(operationStrings.stream().toArray(String[]::new), true));
            operationsStringBuffer.put(chatId, operationStrings);
            operationsDtoBuffer.put(chatId, operations);
        }
        else if (operationsStringBuffer.get(chatId) != null && !operationsStringBuffer.get(chatId).isEmpty()
                && operationsStringBuffer.get(chatId).contains(message)) {
            String[] buttons = new String[]{YES, NO};
            sendMessage.setReplyMarkup(buttonsService.getReplyButtons(buttons, true));
            sendMessage.setText("Хотите удалить операцию?  \n" + message);
            messageService.addLastAnswer(message, chatId);
        }
        else if (operationsStringBuffer.get(chatId).contains(messageService.readLastAnswer(chatId))) {
            String operationString = messageService.readLastAnswer(chatId);
            OperationDTO dto = findOperationDtoByString(operationsDtoBuffer.get(chatId), operationString);
            if (dto == null) {
                sendMessage.setText("Прооизошла какая-то ошибка. Воспользуйтесь меню и попробуйте заново запросить список операций");
                return answerDTO;
            }
            if (message.equals(YES)) {
                if (dto.getOperationName().equals("Доход")) {
                    incomeService.delete(dto.getOperationId());
                    operationsDtoBuffer.get(chatId).remove(dto);
                    operationsStringBuffer.get(chatId).remove(getOperationString(dto));
                    sendMessage.setText("Операция удалена успешно: \n" + operationString);
                    return answerDTO;
                }
                if (dto.getOperationName().equals("Расход")) {
                    expensesService.delete(dto.getOperationId());
                    operationsDtoBuffer.get(chatId).remove(dto);
                    operationsStringBuffer.get(chatId).remove(getOperationString(dto));
                    sendMessage.setText("Операция удалена успешно: \n" + operationString);
                    return answerDTO;
                }
            }
            if (message.equals(NO)) {
                String[] operationStrings = new String[operationsStringBuffer.get(chatId).size()];
                for (int i = 0; i < operationsStringBuffer.get(chatId).size(); i++) {
                    operationStrings[i] = operationsStringBuffer.get(chatId).get(i);
                }
                sendMessage.setReplyMarkup(buttonsService.getReplyButtons(operationStrings, true));
                sendMessage.setText("Ваши операции за последние 7 дней");
                return answerDTO;
            }
        }
        return answerDTO;
    }

    private OperationDTO findOperationDtoByString(List<OperationDTO> operations, String operationString) {
        return operations.stream().filter(operation -> getOperationString(operation).equals(operationString)).findAny().orElse(null);
    }

    private String getOperationString(OperationDTO dto) {
        String operation = "";
        String clientName = "";
        if (!dto.getClient().equals(ClientsService.OTHERS) && !dto.getClient().equals(ClientsService.ENCASHMENT)) {
            clientName = "\nКлиент: " + dto.getClient();
        }
        operation = dto.getOperationName() + " " + dto.getValue() + " " + dto.getPurpose() + clientName;
        return operation.trim();
    }
}
