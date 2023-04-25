package com.disposableemail.telegram.service;

import com.disposableemail.telegram.client.disposableemail.webclient.model.Account;
import com.disposableemail.telegram.client.disposableemail.webclient.model.Credentials;
import com.disposableemail.telegram.client.disposableemail.webclient.model.Domain;
import com.disposableemail.telegram.client.disposableemail.webclient.model.Message;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmailService {

    Flux<Domain> getDomains(@Min(1) Integer size);

    Mono<Account> createAccount(Credentials credentials);

    Mono<Account> deleteAccount(Credentials credentials);

    Flux<Message> getMessages(Credentials credentials, @Min(0) Integer page, @Max(500) Integer size);

}
