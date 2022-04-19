package ru.incomeandexpenses.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.incomeandexpenses.models.Manager;

import java.util.Optional;
import java.util.UUID;

public interface ManagersRepository extends JpaRepository<Manager, UUID> {
    Optional<Manager> findManagerByName(String name);
}
