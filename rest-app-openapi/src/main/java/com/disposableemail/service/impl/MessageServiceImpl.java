package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.dao.entity.MessageEntity;
import com.disposableemail.dao.repository.MessageRepository;
import com.disposableemail.service.api.AccountService;
import com.disposableemail.service.api.MessageService;
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
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final AccountService accountService;

    @Override
    public Mono<MessageEntity> saveMessage(MessageEntity messageEntity) {
        log.info("Saving a Message | Text: {}", messageEntity.getText());

        return messageRepository.save(messageEntity);
    }

    @Override
    public Mono<MessageEntity> getMessageById(String messageId) {
        log.info("Getting a Message | Id {}", messageId);

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
    public Mono<MessageEntity> deleteMessage(String messageId, ServerWebExchange exchange) {
        log.info("Deleting a Message | Id: {}", messageId);

        return accountService.getAccountFromJwt(exchange).map(AccountEntity::getId)
                .flatMap(accountId -> messageRepository.findByIdAndAccountIdAndIsDeletedFalse(messageId, accountId))
                .map(messageEntity -> {
                    messageEntity.setIsDeleted(true);
                    messageRepository.save(messageEntity).subscribe();
                    log.info("Deleted Message | Id: {}", messageEntity.getId());
                    return messageEntity;
                });
    }

    @Override
    public Flux<MessageEntity> getMessages(Integer size, ServerWebExchange exchange) {
        log.info("Getting a Messages collection | Size: {}", size);

        return accountService.getAccountFromJwt(exchange).map(AccountEntity::getId)
                .flatMapMany(accountId ->
                        messageRepository.findByAccountIdAndIsDeletedFalseOrderByCreatedAtDesc(accountId,
                                Pageable.ofSize(size)));
    }

}
