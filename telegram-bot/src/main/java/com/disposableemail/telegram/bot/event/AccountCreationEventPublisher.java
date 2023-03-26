package com.disposableemail.telegram.bot.event;

import com.disposableemail.telegram.client.disposableemail.webclient.model.Credentials;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
@RequiredArgsConstructor
public class AccountCreationEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(@Nullable Credentials credentials) {
        publisher.publishEvent(new AccountCreationEvent(credentials));
    }
}
