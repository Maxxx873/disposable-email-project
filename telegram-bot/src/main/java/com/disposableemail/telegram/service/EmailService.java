package com.disposableemail.telegram.service;

import com.disposableemail.telegram.bot.event.AccountCreationEvent;
import com.disposableemail.telegram.bot.event.AccountDeletionEvent;
import com.disposableemail.telegram.client.disposableemail.webclient.model.Credentials;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmailService {

    Flux<String> getDomains(@Min(1) Integer size);

    Mono<String> getDomain(String id);

    Mono<String> getToken(Credentials credentials);

    Flux<String> getMessages(@Min(0) Integer page, @Max(500) Integer size);

    Mono<String> getMe();

    void createAccount(AccountCreationEvent event);

    void deleteAccount(AccountDeletionEvent event);

}
