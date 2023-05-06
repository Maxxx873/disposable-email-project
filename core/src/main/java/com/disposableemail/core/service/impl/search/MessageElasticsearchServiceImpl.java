package com.disposableemail.core.service.impl.search;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.dao.entity.search.MessageElasticsearchEntity;
import com.disposableemail.core.dao.repository.search.MessageElasticsearchRepository;
import com.disposableemail.core.event.Event;
import com.disposableemail.core.event.EventProducer;
import com.disposableemail.core.security.UserCredentials;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.api.search.MessageElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.disposableemail.core.security.SecurityUtils.getCredentialsFromJwt;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.service", havingValue = "elasticsearch")
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
                })
                .filter(messageElasticsearchEntity -> !messageElasticsearchEntity.getIsDeleted());
    }

    @Override
    public Flux<MessageElasticsearchEntity> getMessagesFromMailbox(Pageable pageable, ServerWebExchange exchange) {

        return getAccountEntityMono()
                .map(accountEntity -> {
                            eventProducer.send(new Event<>(Event.Type.GETTING_MESSAGES, accountEntity));
                            return accountEntity;
                        }
                )
                .flatMapMany(accountEntity -> {
                    log.info("Getting a Messages collection from Elasticsearch | mailboxId: {}", accountEntity.getMailboxId());
                    return messageElasticsearchRepository.findByMailboxId(accountEntity.getMailboxId(), pageable)
                            .map(message -> {
                                message.setAccountId(accountEntity.getId());
                                return message;
                            })
                            .filter(messageElasticsearchEntity -> !messageElasticsearchEntity.getIsDeleted());
                });
    }

    @Override
    public Mono<MessageElasticsearchEntity> getMessage(String messageId, ServerWebExchange exchange) {
        log.info("Getting a Message from Elasticsearch | id: {}", messageId);

        return getAccountEntityMono()
                .map(AccountEntity::getAddress)
                .flatMap(address -> messageElasticsearchRepository.findByAddressToAndMessageId(address, messageId));
    }

    @Override
    public Mono<Void> deleteMessageById(String messageId) {
        log.info("Deleting a Message in Elasticsearch | messageId: {}", messageId);

        messageElasticsearchRepository.deleteByMessageId(messageId)
                .doOnSuccess(result -> log.info("Message deleted in Elasticsearch | messageId: {}", messageId))
                .subscribe();
        return Mono.empty();
    }

    @Override
    public Mono<MessageElasticsearchEntity> softDeleteMessageById(String messageId) {
        log.info("Soft deleting a Message in Elasticsearch | messageId: {}", messageId);

        var result = messageElasticsearchRepository.findByMessageId(messageId)
                .map(messageElasticsearchEntity -> {
                    log.warn("Set deleted a Message in Elasticsearch | messageId: {}", messageId);
                    messageElasticsearchEntity.setIsDeleted(true);
                    return messageElasticsearchEntity;
                })
                .flatMap(messageElasticsearchRepository::save);
        result.subscribe();
        return result;
    }

    private Mono<AccountEntity> getAccountEntityMono() {
        return getCredentialsFromJwt().map(UserCredentials::getPreferredUsername)
                .map(accountService::getAccountByAddress)
                .flatMap(accountEntityMono -> accountEntityMono);
    }
}
