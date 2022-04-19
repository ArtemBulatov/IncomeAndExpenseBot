package ru.incomeandexpenses.services;

import org.springframework.stereotype.Service;
import ru.incomeandexpenses.models.Verify;
import ru.incomeandexpenses.repositories.VerifyRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class VerifyService {
    private final VerifyRepository verifyRepository;

    public VerifyService(VerifyRepository verifyRepository) {
        this.verifyRepository = verifyRepository;
    }

    public Verify createNewVerify() {
        Verify verify = new Verify();
        verify.setId(UUID.randomUUID());
        verify.setDate(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        return verifyRepository.save(verify);
    }

    public Verify getLast() {
        return verifyRepository.findLastByOrderByDate();
    }
}
