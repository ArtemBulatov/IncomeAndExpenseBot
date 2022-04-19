package ru.incomeandexpenses.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.incomeandexpenses.models.Salary;
import java.util.UUID;

public interface SalariesRepository extends JpaRepository<Salary, UUID> {
}
