package ru.incomeandexpenses.services;

import org.springframework.stereotype.Service;
import ru.incomeandexpenses.models.Manager;
import ru.incomeandexpenses.repositories.ManagersRepository;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ManagersService {
    private final ManagersRepository managersRepository;

    public ManagersService(ManagersRepository managersRepository) {
        this.managersRepository = managersRepository;
    }

    public Manager save(String managerName) {
        Manager manager = new Manager();
        manager.setId(UUID.randomUUID());
        manager.setName(managerName);
        manager.setInArchive(false);
        return managersRepository.save(manager);
    }

    public Manager addManagerInArchive(Manager manager) {
        manager.setInArchive(true);
        return managersRepository.save(manager);
    }

    public Manager get(UUID id) {
        return managersRepository.getById(id);
    }

    public Manager get(String managerName) {
        return managersRepository.findManagerByName(managerName).orElse(null);
    }

    public String[] getManagersNames() {
        List<Manager> managers = getAll();
        managers.sort(Comparator.comparing(Manager::getName));
        String[] managersNames = new String[managers.size()];
        for (int i = 0; i < managers.size(); i++) {
            managersNames[i] = managers.get(i).getName();
        }
        return managersNames;
    }

    public List<Manager> getAll() {
        return managersRepository.findAll().stream()
                .filter(manager -> !manager.isInArchive()).collect(Collectors.toList());
    }
}
