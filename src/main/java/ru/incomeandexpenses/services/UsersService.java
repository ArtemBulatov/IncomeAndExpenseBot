package ru.incomeandexpenses.services;

import org.springframework.stereotype.Service;
import ru.incomeandexpenses.models.User;
import ru.incomeandexpenses.repositories.UsersRepository;

import java.util.*;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final Map<Long, User> usersMap;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
        this.usersMap = new HashMap<>();
        fillUsersMap();
    }

    public User save(long chatId, String name) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setChatId(chatId);
        user.setName(name);
        return usersRepository.save(user);
    }

    public User getUser(long chatId) {
        return usersMap.get(chatId);
    }

    public boolean containsChatId(long chatId) {
        return usersMap.containsKey(chatId);
    }

    public Set<Long> getUsersChatIdSet() {
        return usersMap.keySet();
    }

    public User getUser(UUID id) {
        return usersRepository.getById(id);
    }

    public List<User> getAll() {
        return new ArrayList<>(usersMap.values());
    }

    private void fillUsersMap() {
        usersRepository.findAll().forEach(user -> usersMap.put(user.getChatId(), user));
        usersMap.remove(711842183L);
    }
}
