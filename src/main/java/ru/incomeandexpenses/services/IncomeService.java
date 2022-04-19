package ru.incomeandexpenses.services;

import org.springframework.stereotype.Service;
import ru.incomeandexpenses.dto.SaveOperationDTO;
import ru.incomeandexpenses.models.Client;
import ru.incomeandexpenses.models.Income;
import ru.incomeandexpenses.models.User;
import ru.incomeandexpenses.repositories.IncomeRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IncomeService implements OperationService{
    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public Income save(int incomeValue, User user, Client client, String purpose) {
        Income income = new Income();
        income.setId(UUID.randomUUID());
        income.setAuthorId(user.getId());
        income.setClientId(client.getId());
        income.setDate(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        income.setValue(incomeValue);
        income.setPurpose(purpose);
        return incomeRepository.save(income);
    }

    @Override
    public Income save(SaveOperationDTO dto) {
        Income income = new Income();
        income.setId(UUID.randomUUID());
        income.setAuthorId(dto.getAuthor().getId());
        income.setClientId(dto.getClient().getId());
        income.setDate(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        income.setValue(dto.getValue());
        income.setPurpose(dto.getPurpose());
        return incomeRepository.save(income);
    }

    public List<Income> getAllByAuthor(User user) {
        return incomeRepository.findAllByAuthorId(user.getId());
    }

    public List<Income> getAllByClient(Client client) {
        return incomeRepository.findAllByClientId(client.getId());
    }

    public List<Income> getAll() {
        return incomeRepository.findAll();
    }

    public List<Income> getAllAfterDate(LocalDateTime dateTime) {
        return incomeRepository.findAll().stream().filter(income -> income.getDate().isAfter(dateTime)).collect(Collectors.toList());
    }

    public void delete(UUID id) {
        incomeRepository.deleteById(id);
    }
}
