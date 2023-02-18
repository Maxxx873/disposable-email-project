package com.disposableemail.core.service.impl;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.dao.entity.MessageEntity;
import com.disposableemail.core.dao.entity.search.MessageElasticsearchEntity;
import com.disposableemail.core.dao.repository.MessageRepository;
import com.disposableemail.core.dao.repository.search.MessageElasticsearchRepository;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.api.MessageService;
import com.disposableemail.core.service.api.search.MessageElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageElasticsearchService messageElasticsearchService;
    private final AccountService accountService;

    private final MessageElasticsearchRepository elasticsearchRepository;

    @Override
    public Mono<MessageEntity> saveMessage(MessageEntity messageEntity) {
        log.info("Updating a Message | Text: {}", messageEntity.getText());

        return messageRepository.save(messageEntity);
    }

    @Override
    public Mono<MessageEntity> getMessageById(String messageId) {
        log.info("Getting a Message | Id: {}", messageId);

        return messageRepository.findById(messageId);
    }

    @Override
    public Mono<MessageEntity> getMessage(String messageId, ServerWebExchange exchange) {
        log.info("Getting a Message | Id {}", messageId);

        return accountService.getAccountFromJwt(exchange).map(AccountEntity::getId)
                .flatMap(accountId -> messageRepository.findByIdAndAccountIdAndIsDeletedFalse(messageId, accountId));
    }

    @Override
    public Mono<MessageEntity> updateMessage(String messageId, MessageEntity messageEntity, ServerWebExchange exchange) {
        log.info("Updating a Message | Id: {}", messageId);

        return accountService.getAccountFromJwt(exchange).map(AccountEntity::getId)
                .flatMap(accountId -> messageRepository.findByIdAndAccountIdAndIsDeletedFalse(messageId, accountId))
                .map(message -> {
                    messageRepository.save(messageEntity).subscribe();
                    log.info("Updated Message | Id: {}", messageEntity.getId());
                    return messageEntity;
                });
    }

    @Override
    @Transactional
    public Mono<MessageEntity> softDeleteMessage(String messageId, ServerWebExchange exchange) {
        log.info("Deleting a Message | Id: {}", messageId);

        return accountService.getAccountFromJwt(exchange).map(AccountEntity::getId)
                .flatMap(accountId -> messageRepository.findByIdAndAccountIdAndIsDeletedFalse(messageId, accountId))
                .map(messageEntity -> {
                    messageEntity.setIsDeleted(true);
                    messageRepository.save(messageEntity).subscribe();

                    elasticsearchRepository.findByMessageId(messageId)
                            .map(MessageElasticsearchEntity::getId)
                            .map(Collections::singletonList)
                            .flatMap(elasticsearchRepository::deleteAllById)
                            .doOnSuccess(result -> log.info("Deleted Message in ElasticSearch | Id: {}", messageEntity.getId()))
                            .subscribe();
                    return messageEntity;
                });
    }

    @Override
    public Flux<MessageEntity> getMessages(Integer size, ServerWebExchange exchange) {
        log.info("Getting a Messages collection | Size: {}", size);

        return accountService.getAccountFromJwt(exchange)
                .map(AccountEntity::getId)
                .flatMapMany(accountId ->
                        messageRepository.findByAccountIdAndIsDeletedFalseOrderByCreatedAtDesc(accountId,
                                Pageable.ofSize(size)));
    }

}
