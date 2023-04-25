package com.disposableemail.telegram.service.impl;

import com.disposableemail.telegram.client.disposableemail.webclient.api.DefaultApi;
import com.disposableemail.telegram.client.disposableemail.webclient.model.*;
import com.disposableemail.telegram.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisposableEmailServiceImpl implements EmailService {

    private final DefaultApi api;


    @Override
    public Flux<Domain> getDomains(Integer size) {
        return api.getDomainCollection(size);
    }

    @Override
    public Flux<Message> getMessages(Credentials credentials, Integer page, Integer size) {
        return getTokenMono(credentials).flatMapMany(token -> {
            api.getApiClient().setBearerToken(token.getToken());
            return api.getMessageCollection(page, size)
                    .map(Messages::getId)
                    .flatMap(api::getMessageItem);
        });
    }

    @Override
    public Mono<Account> createAccount(Credentials credentials) {
        return api.createAccountItem(credentials);
    }

    @Override
    public Mono<Account> deleteAccount(Credentials credentials) {
        return getTokenMono(credentials).map(token -> {
            api.getApiClient().setBearerToken(token.getToken());
            var accountItem = api.getMeAccountItem();
            accountItem.map(Account::getId).flatMap(api::deleteAccountItem).subscribe();
            return accountItem;
        }).flatMap(account -> account);
    }

    private Mono<Token> getTokenMono(Credentials credentials) {
        return api.postCredentialsItem(credentials);
    }

}
