package com.disposableemail.telegram.bot.event;

import com.disposableemail.telegram.client.disposableemail.webclient.model.Credentials;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
@RequiredArgsConstructor
public class AccountDeletionEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(@Nullable Credentials credentials) {
        publisher.publishEvent(new AccountDeletionEvent(credentials));
    }
}
