package com.disposableemail.telegram.bot.listener;

import com.disposableemail.telegram.bot.event.AccountCreationEvent;
import com.disposableemail.telegram.bot.event.AccountDeletionEvent;
import com.disposableemail.telegram.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotListener {

    private final EmailService emailService;

    @Async
    @EventListener(AccountCreationEvent.class)
    public void createAccount(AccountCreationEvent event) {
        var credentials = event.get();
        if (!Objects.equals(credentials, null)) {
            log.info("Account creation event | {}", credentials.getAddress());
            emailService.createAccount(credentials);
        }
    }

    @Async
    @EventListener(AccountDeletionEvent.class)
    public void deleteAccount(AccountDeletionEvent event) {
        var credentials = event.get();
        if (!Objects.equals(credentials, null)) {
            log.info("Account deletion event | {}", credentials.getAddress());
            emailService.deleteAccount(credentials);
        }
    }

}
