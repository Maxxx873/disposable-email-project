package com.disposableemail.telegram.service.impl;

import com.disposableemail.telegram.client.disposableemail.webclient.api.DefaultApi;
import com.disposableemail.telegram.client.disposableemail.webclient.model.*;
import com.disposableemail.telegram.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @Override
    public Flux<Domain> getDomains(Integer size) {
        return api.getDomainCollection(size);
    }

    @Override
    public Mono<String> getDomain(String id) {
        return api.getDomainItem(id).map(this::apply);
    }

    @Override
    public Mono<Message> getMessage(String messageId) {
        return api.getMessageItem(messageId);
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
    public Mono<String> getToken(Credentials credentials) {
        return getTokenMono(credentials).map(Token::getToken);
    }

    @Override
    public Mono<String> getMe() {
        return api.getMeAccountItem().map(this::apply);
    }

    @Override
    public void createAccount(Credentials credentials) {
        api.createAccountItem(credentials).subscribe();
    }

    @Override
    public void deleteAccount(Credentials credentials) {
        getTokenMono(credentials).map(token -> {
            api.getApiClient().setBearerToken(token.getToken());
            return api.getMeAccountItem().map(Account::getId).flatMap(api::deleteAccountItem).subscribe();
        }).subscribe();
    }

    private Mono<Token> getTokenMono(Credentials credentials) {
        return api.postCredentialsItem(credentials);
    }

    private String apply(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error(e.getLocalizedMessage());
            return e.getLocalizedMessage();
        }
    }
}
