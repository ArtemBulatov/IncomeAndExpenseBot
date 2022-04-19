package ru.incomeandexpenses.services;

import org.springframework.stereotype.Service;
import ru.incomeandexpenses.models.Client;
import ru.incomeandexpenses.models.Salary;
import ru.incomeandexpenses.models.User;
import ru.incomeandexpenses.repositories.SalariesRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class SalariesService {
    private final SalariesRepository salariesRepository;

    public SalariesService(SalariesRepository salariesRepository) {
        this.salariesRepository = salariesRepository;
    }

    public Salary save(int salaryValue, User user, Client client, String purpose) {
        Salary salary = new Salary();
        salary.setId(UUID.randomUUID());
        salary.setAuthorId(user.getId());
        salary.setClientId(client.getId());
        salary.setDate(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        salary.setValue(salaryValue);
        salary.setPurpose(purpose);
        return salariesRepository.save(salary);
    }

}
