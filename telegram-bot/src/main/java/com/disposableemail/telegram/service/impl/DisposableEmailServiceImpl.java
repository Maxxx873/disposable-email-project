package com.disposableemail.telegram.service.impl;

import com.disposableemail.telegram.bot.event.AccountCreationEvent;
import com.disposableemail.telegram.bot.event.AccountDeletionEvent;
import com.disposableemail.telegram.client.disposableemail.webclient.api.DefaultApi;
import com.disposableemail.telegram.client.disposableemail.webclient.model.*;
import com.disposableemail.telegram.dto.MessageDto;
import com.disposableemail.telegram.dto.MessageMapper;
import com.disposableemail.telegram.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisposableEmailServiceImpl implements EmailService {

    private final DefaultApi api;
    private final ObjectMapper objectMapper;
    private final MessageMapper mapper;

    @Override
    public Flux<String> getDomains(Integer size) {
        return api.getDomainCollection(size).map(Domain::getDomain);
    }

    @Override
    public Mono<String> getDomain(String id) {
        return api.getDomainItem(id).map(this::apply);
    }

    @Override
    public Flux<String> getMessages(Integer page, Integer size) {
        return api.getMessageCollection(page, size)
                .map(Messages::getId)
                .flatMap(api::getMessageItem)
                .map(mapper::messageToDto)
                .map(MessageDto::toString);
    }

    @Override
    public Mono<String> getToken(Credentials credentials) {
        return api.postCredentialsItem(credentials).map(Token::getToken);
    }

    @Override
    public Mono<String> getMe() {
        return api.getMeAccountItem().map(this::apply);
    }

    @Async
    @Override
    @EventListener(AccountCreationEvent.class)
    public void createAccount(AccountCreationEvent event) {
        var credentials = event.get();
        if (!Objects.equals(credentials, null)) {
            log.info("Account creation event | {}", credentials.getAddress());
            api.createAccountItem(credentials).subscribe();
        }
    }

    @Async
    @Override
    @EventListener(AccountDeletionEvent.class)
    public void deleteAccount(AccountDeletionEvent event) {
        var credentials = event.get();
        if (!Objects.equals(credentials, null)) {
            log.info("Account deletion event | {}", credentials.getAddress());
            api.getApiClient().setBearerToken(api.postCredentialsItem(credentials).map(Token::getToken).block());
            api.getMeAccountItem().map(Account::getId).flatMap(api::deleteAccountItem).subscribe();
        }
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
