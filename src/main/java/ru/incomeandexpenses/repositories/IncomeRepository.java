package ru.incomeandexpenses.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.incomeandexpenses.models.Income;
import java.util.List;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Income, UUID> {
    List<Income> findAllByAuthorId(UUID uuid);
    List<Income> findAllByClientId(UUID uuid);
}
