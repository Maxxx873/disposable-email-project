package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.dao.entity.MessageEntity;
import com.disposableemail.dao.repository.MessageRepository;
import com.disposableemail.service.api.AccountService;
import com.disposableemail.service.api.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final AccountService accountService;

    @Override
    public Flux<MessageEntity> getMessages(ServerWebExchange exchange) {
        log.info("Getting a Messages collection | Size {}", 1);

        return accountService.getAccountFromJwt(exchange)
                .flux().map(AccountEntity::getAddress)
                .flatMap(messageRepository::findByAddressTo);
    }

    @Override
    public Mono<MessageEntity> getMessage(String id, ServerWebExchange exchange) {
        log.info("Getting a Message {}", id);

        return  accountService.getAccountFromJwt(exchange)
                .map(AccountEntity::getAddress)
                .flatMap(address -> messageRepository.findByAddressToAndMessageId(address, id));
    }
}
