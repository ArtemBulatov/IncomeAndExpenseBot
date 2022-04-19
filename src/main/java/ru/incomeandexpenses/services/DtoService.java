package ru.incomeandexpenses.services;

import org.springframework.stereotype.Service;
import ru.incomeandexpenses.dto.OperationDTO;
import ru.incomeandexpenses.models.Client;
import ru.incomeandexpenses.models.Expense;
import ru.incomeandexpenses.models.Income;
import ru.incomeandexpenses.models.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DtoService {
    private final UsersService usersService;
    private final ClientsService clientsService;
    private final IncomeService incomeService;
    private final ExpensesService expensesService;

    public DtoService(UsersService usersService, ClientsService clientsService, IncomeService incomeService, ExpensesService expensesService) {
        this.usersService = usersService;
        this.clientsService = clientsService;
        this.incomeService = incomeService;
        this.expensesService = expensesService;
    }

    public List<OperationDTO> getAllIncomeDtoList(LocalDateTime date) {
        Map<UUID, User> users = new HashMap<>();
        usersService.getAll().forEach(user -> users.put(user.getId(), user));

        Map<UUID, Client> clients= new HashMap<>();
        clientsService.getClients().forEach(client -> clients.put(client.getId(), client));

        List<OperationDTO> dtoList = new ArrayList<>();
        List<Income> incomeList = incomeService.getAll();
        incomeList.sort(Collections.reverseOrder(Comparator.comparing(Income::getDate)));
        incomeList.forEach(income -> {
            if (income.getDate().isAfter(date)) {
                OperationDTO dto = new OperationDTO();
                dto.setOperationId(income.getId());
                dto.setOperationName("Доход");
                dto.setValue(income.getValue());
                dto.setDate(income.getDate());
                dto.setStringDate(income.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                dto.setAuthor(users.get(income.getAuthorId()).getName());
                dto.setClient(clients.get(income.getClientId()).getName());
                String purpose = "";
                if (income.getPurpose() != null) {
                    purpose = income.getPurpose();
                }
                dto.setPurpose(purpose);
                dtoList.add(dto);
            }
        });
        return dtoList;
    }

    public List<OperationDTO> getAllExpenseDtoList(LocalDateTime date) {
        Map<UUID, User> users = new HashMap<>();
        usersService.getAll().forEach(user -> users.put(user.getId(), user));

        Map<UUID, Client> clients= new HashMap<>();
        clientsService.getClients().forEach(client -> clients.put(client.getId(), client));

        List<OperationDTO> dtoList = new ArrayList<>();
        List<Expense> expenseList = expensesService.getAll();
        expenseList.sort(Collections.reverseOrder(Comparator.comparing(Expense::getDate)));
        expenseList.forEach(expense -> {
            if (expense.getDate().isAfter(date)) {
                OperationDTO dto = new OperationDTO();
                dto.setOperationId(expense.getId());
                dto.setOperationName("Расход");
                dto.setValue(expense.getValue());
                dto.setDate(expense.getDate());
                dto.setStringDate(expense.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                dto.setAuthor(users.get(expense.getAuthorId()).getName());
                dto.setClient(clients.get(expense.getClientId()).getName());
                String purpose = "";
                if (expense.getPurpose() != null) {
                    purpose = expense.getPurpose();
                }
                dto.setPurpose(purpose);
                dtoList.add(dto);
            }
        });
        return dtoList;
    }

    public List<OperationDTO> getOperationDtoListForUser(LocalDateTime date, User user) {
        List<OperationDTO> operations = new ArrayList<>();
        operations.addAll(getAllIncomeDtoList(date).stream()
                .filter(dto -> dto.getAuthor().equals(user.getName())).toList());
        operations.addAll(getAllExpenseDtoList(date).stream()
                .filter(dto -> dto.getAuthor().equals(user.getName())).toList());
        operations.sort(Collections.reverseOrder(Comparator.comparing(OperationDTO::getDate)));
        return operations;
    }
}
