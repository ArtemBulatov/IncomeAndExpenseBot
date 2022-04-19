package ru.incomeandexpenses.services;

import org.springframework.stereotype.Service;
import ru.incomeandexpenses.dto.SaveOperationDTO;
import ru.incomeandexpenses.models.Client;
import ru.incomeandexpenses.models.Expense;
import ru.incomeandexpenses.models.User;
import ru.incomeandexpenses.repositories.ExpensesRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpensesService implements OperationService{
    private final ExpensesRepository expensesRepository;

    public ExpensesService(ExpensesRepository expensesRepository) {
        this.expensesRepository = expensesRepository;
    }

    public Expense save(int expenseValue, User user, Client client, String purpose) {
        Expense expense = new Expense();
        expense.setId(UUID.randomUUID());
        expense.setAuthorId(user.getId());
        expense.setClientId(client.getId());
        expense.setDate(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        expense.setValue(expenseValue);
        expense.setPurpose(purpose);
        return expensesRepository.save(expense);
    }

    @Override
    public Expense save(SaveOperationDTO dto) {
        Expense expense = new Expense();
        expense.setId(UUID.randomUUID());
        expense.setAuthorId(dto.getAuthor().getId());
        expense.setClientId(dto.getClient().getId());
        expense.setDate(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        expense.setValue(dto.getValue());
        expense.setPurpose(dto.getPurpose());
        return expensesRepository.save(expense);
    }

    public List<Expense> getAllByAuthor(User user) {
        return expensesRepository.findAllByAuthorId(user.getId());
    }

    public List<Expense> getAllByClient(Client client) {
        return expensesRepository.findAllByClientId(client.getId());
    }

    public List<Expense> getAll() {
        return expensesRepository.findAll();
    }

    public List<Expense> getAllAfterDate(LocalDateTime dateTime) {
        return expensesRepository.findAll().stream().filter(expense -> expense.getDate().isAfter(dateTime)).collect(Collectors.toList());
    }

    public void delete(UUID id) {
        expensesRepository.deleteById(id);
    }
}
