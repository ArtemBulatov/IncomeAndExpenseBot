package ru.incomeandexpenses.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.incomeandexpenses.models.Verify;
import java.util.UUID;

public interface VerifyRepository extends JpaRepository<Verify, UUID> {
    Verify findLastByOrderByDate();
}
