package ru.incomeandexpenses.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.incomeandexpenses.models.Client;
import java.util.List;
import java.util.UUID;

public interface ClientsRepository extends JpaRepository<Client, UUID> {
    List<Client> findAllByInArchive(boolean isInArchive);
    List<Client> findAllByName(String name);
    Client findClientByName(String name);
    List<Client> findAllByManagerId(UUID id);
}
