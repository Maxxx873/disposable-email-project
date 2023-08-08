package com.disposableemail.core.service.impl;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.dao.entity.MessageEntity;
import com.disposableemail.core.dao.repository.MessageRepository;
import com.disposableemail.core.security.UserCredentials;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.api.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.disposableemail.core.security.SecurityUtils.getCredentialsFromJwt;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final String ACCESS_DENIED_MESSAGE = "Account unauthorized";

    private final MessageRepository messageRepository;
    private final AccountService accountService;

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
    public Mono<MessageEntity> getMessage(String messageId) {
        log.info("Getting a Message | Id {}", messageId);

        return getAccountEntityMono().map(AccountEntity::getId)
                .flatMap(accountId -> messageRepository.findByIdAndAccountIdAndIsDeletedFalse(messageId, accountId))
                .switchIfEmpty(Mono.error(new AccessDeniedException(ACCESS_DENIED_MESSAGE)));
    }

    @Override
    public Mono<MessageEntity> updateMessage(String messageId, MessageEntity messageEntity) {
        log.info("Updating a Message | Id: {}", messageId);

        return getAccountEntityMono().map(AccountEntity::getId)
                .flatMap(accountId -> messageRepository.findByIdAndAccountIdAndIsDeletedFalse(messageId, accountId))
                .switchIfEmpty(Mono.error(new AccessDeniedException(ACCESS_DENIED_MESSAGE)))
                .map(message -> {
                    messageRepository.save(messageEntity).subscribe();
                    log.info("Updated Message | Id: {}", messageEntity.getId());
                    return messageEntity;
                });
    }

    @Override
    @Transactional
    public Mono<MessageEntity> softDeleteMessage(String messageId) {
        log.info("Deleting a Message | Id: {}", messageId);

        return getAccountEntityMono().map(AccountEntity::getId)
                .flatMap(accountId -> messageRepository.findByIdAndAccountIdAndIsDeletedFalse(messageId, accountId))
                .switchIfEmpty(Mono.error(new AccessDeniedException(ACCESS_DENIED_MESSAGE)))
                .map(messageEntity -> {
                    messageEntity.setIsDeleted(true);
                    messageRepository.save(messageEntity).subscribe();
                    return messageEntity;
                });
    }

    @Override
    public Flux<MessageEntity> getMessagesForAuthorizedAccount(Pageable pageable) {
        return getAccountEntityMono()
                .switchIfEmpty(Mono.error(new AccessDeniedException(ACCESS_DENIED_MESSAGE)))
                .flatMapMany(accountEntity -> {
                    log.info("Getting a Messages collection | mailboxId: {}", accountEntity.getMailboxId());
                    return messageRepository.findByAccountIdAndIsDeletedFalseOrderByCreatedAtDesc(accountEntity.getId(), pageable);
                });
    }

    @Override
    public Mono<Void> deleteMessagesOlderNumberOfDays(Integer days) {
        Instant cutOffDate = Instant.now().minus(days, ChronoUnit.DAYS);
        return messageRepository.deleteByCreatedAtBefore(cutOffDate);
    }

    private Mono<AccountEntity> getAccountEntityMono() {
        return getCredentialsFromJwt().map(UserCredentials::getPreferredUsername).map(accountService::getAccountByAddress)
                .switchIfEmpty(Mono.error(new AccessDeniedException(ACCESS_DENIED_MESSAGE)))
                .flatMap(accountEntityMono -> accountEntityMono);
    }
}
