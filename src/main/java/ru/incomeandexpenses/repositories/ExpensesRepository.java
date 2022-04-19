package ru.incomeandexpenses.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.incomeandexpenses.models.Expense;
import java.util.List;
import java.util.UUID;

public interface ExpensesRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findAllByAuthorId(UUID uuid);
    List<Expense> findAllByClientId(UUID uuid);
}
