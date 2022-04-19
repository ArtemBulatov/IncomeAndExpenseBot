package ru.incomeandexpenses.services;

import org.springframework.stereotype.Service;
import ru.incomeandexpenses.models.Client;
import ru.incomeandexpenses.repositories.ClientsRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientsService {
    public static final String OTHERS = "Прочее";
    public static final String ENCASHMENT = "Инкассация";
    private final ClientsRepository clientsRepository;

    public ClientsService(ClientsRepository clientsRepository) {
        this.clientsRepository = clientsRepository;
        initSystemClients();
    }

    public Client save(String clientName, UUID managerId) {
        Client client = getClientByName(clientName);
        if (client == null) {
            client = new Client();
            client.setId(UUID.randomUUID());
            client.setDate(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            client.setInArchive(false);
        }
        client.setName(clientName);
        client.setManagerId(managerId);

        return clientsRepository.save(client);
    }

    public Client getClient(UUID id) {
        return clientsRepository.getById(id);
    }

    public Client addClientInArchive(Client clientToArchive) {
        clientToArchive.setInArchive(true);
        return clientsRepository.save(clientToArchive);
    }

    public List<Client> getClientsByPartOfName(String partOfName) {
        return clientsRepository.findAll().stream()
                .filter(client -> client.getName().toLowerCase(Locale.ROOT).contains(partOfName.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    public Client getClientByName(String name) {
        return clientsRepository.findClientByName(name);
    }

    public List<Client> getClientsByManagerId(UUID id) {
        return clientsRepository.findAllByManagerId(id);
    }

    public List<Client> getSortedClientsNotInArchive() {
        return clientsRepository.findAllByInArchive(false)
                .stream().sorted(Collections.reverseOrder(Comparator.comparing(Client::getDate)))
                .collect(Collectors.toList());
    }

    public List<Client> getClientListToButtons() {
        return getSortedClientsNotInArchive().stream()
                .filter(client -> !client.getName().equals(OTHERS) && !client.getName().equals(ENCASHMENT))
                .toList();
    }

    public List<Client> getClients() {
        return clientsRepository.findAll();
    }

    private void initSystemClients() {
        init(OTHERS);
        init(ENCASHMENT);
    }

    private void init(String name) {
        if (clientsRepository.findAllByName(name).isEmpty()) {
            Client client = new Client();
            client.setId(UUID.randomUUID());
            client.setName(name);
            client.setDate(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
            client.setInArchive(false);
            clientsRepository.save(client);
        }
    }

}
