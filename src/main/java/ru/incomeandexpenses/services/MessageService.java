package ru.incomeandexpenses.services;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.*;

@Service
public class MessageService {

    private final Map<Long, LinkedList<String>> lastMessageMap;
    private final Map<Long, LinkedList<String>> lastAnswerMap;
    private final UsersService usersService;

    public MessageService(UsersService usersService) {
        this.usersService = usersService;
        this.lastMessageMap = new HashMap<>();
        this.lastAnswerMap = new HashMap<>();
        initMaps();
    }

    public void addLastAnswer(String message, long chatId) {
        lastAnswerMap.get(chatId).add(message);
    }

    public String readLastAnswer(long chatId) {
        return lastAnswerMap.get(chatId).getLast();
    }

    public String deleteLastAnswer(long chatId) {
        return lastAnswerMap.get(chatId).removeLast();
    }

    public boolean answersIsEmpty(long chatId) {
        return lastAnswerMap.get(chatId).isEmpty();
    }

    public int getSizeOfAnswers(long chatId) {
        return lastAnswerMap.get(chatId).size();
    }

    public LinkedList<String> getAnswerList(long chatId) {
        return lastAnswerMap.get(chatId);
    }

    public void clearAnswers(long chatId) {
        lastAnswerMap.put(chatId, new LinkedList<>());
    }

    private void checkSizeOfAnswerList(long userChatId) {
        if (lastAnswerMap.get(userChatId).size() > 15) {
            for (int i = 0; i < 5; i++) {
                lastAnswerMap.get(userChatId).removeFirst();
            }
        }
    }



    public void addLastMessage(String message, long chatId) {
        lastMessageMap.get(chatId).add(message);
    }

    public String readLastMessage(long chatId) {
        return lastMessageMap.get(chatId).getLast();
    }

    public String deleteLastMessage(long chatId) {
        return lastMessageMap.get(chatId).removeLast();
    }

    public boolean messagesIsEmpty(long chatId) {
        return lastMessageMap.get(chatId).size() == 0;
    }

    public int getSizeOfMessages(long chatId) {
        return lastMessageMap.get(chatId).size();
    }

    public LinkedList<String> getMessageList(long chatId) {
        return lastMessageMap.get(chatId);
    }

    public void clearMessages(long chatId) {
        lastMessageMap.put(chatId, new LinkedList<>());
    }

    private void initMaps() {
        usersService.getAll().forEach(user -> {
            lastMessageMap.put(user.getChatId(), new LinkedList<>());
            lastAnswerMap.put(user.getChatId(), new LinkedList<>());
        });
    }


    public List<SendMessage> getMessagesToAnotherUsers(String message, long thisUserChatId) {
        List<SendMessage> messages = new ArrayList<>();
        Set<Long> anotherUsers = new HashSet<>(usersService.getUsersChatIdSet());
        anotherUsers.remove(thisUserChatId);
        anotherUsers.forEach(usersChatId -> {
            SendMessage notifyMessage = new SendMessage();
            notifyMessage.setChatId(String.valueOf(usersChatId));
            notifyMessage.setText(message);
            messages.add(notifyMessage);
        });
        return messages;
    }
}
