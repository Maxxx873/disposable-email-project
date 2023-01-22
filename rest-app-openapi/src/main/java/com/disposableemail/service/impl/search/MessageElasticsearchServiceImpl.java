package com.disposableemail.service.impl.search;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.dao.entity.search.MessageElasticsearchEntity;
import com.disposableemail.dao.repository.search.MessageElasticsearchRepository;
import com.disposableemail.event.EventProducer;
import com.disposableemail.service.api.AccountService;
import com.disposableemail.service.api.search.MessageElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class MessageElasticsearchServiceImpl implements MessageElasticsearchService {

    private final MessageElasticsearchRepository messageElasticsearchRepository;
    private final AccountService accountService;
    private final EventProducer eventProducer;


    @Override
    public Flux<MessageElasticsearchEntity> getMessagesFromMailbox(AccountEntity accountEntity) {

        return messageElasticsearchRepository.findByMailboxId(accountEntity.getMailboxId())
                .map(message -> {
                    message.setAccountId(accountEntity.getId());
                    log.info("Getting a Messages collection from Elasticsearch | mailboxId: {}", message.getMailboxId());
                    return message;
                });
    }

    @Override
    public Flux<MessageElasticsearchEntity> getMessagesFromMailbox(Pageable pageable, ServerWebExchange exchange) {

        return accountService.getAccountFromJwt(exchange)
                .flatMapMany(accountEntity -> {
                    log.info("Getting a Messages collection from Elasticsearch | mailboxId: {}", accountEntity.getMailboxId());
                    return messageElasticsearchRepository.findByMailboxId(accountEntity.getMailboxId(), pageable)
                            .map(message -> {
                                message.setAccountId(accountEntity.getId());
                                return message;
                            });
                });
    }

    @Override
    public Mono<MessageElasticsearchEntity> getMessage(String id, ServerWebExchange exchange) {
        log.info("Getting a Message from Elasticsearch | id: {}", id);
        return accountService.getAccountFromJwt(exchange)
                .map(AccountEntity::getAddress)
                .flatMap(address -> messageElasticsearchRepository.findByAddressToAndMessageId(address, id));
    }

}