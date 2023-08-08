package com.disposableemail.core.service.api;

import com.disposableemail.core.dao.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageService {
    Mono<MessageEntity> getMessage(String messageId);

    Mono<MessageEntity> saveMessage(MessageEntity messageEntity);

    Mono<MessageEntity> getMessageById(String messageId);

    Mono<MessageEntity> softDeleteMessage(String messageId);

    Mono<MessageEntity> updateMessage(String messageId, MessageEntity messageEntity);

    Flux<MessageEntity> getMessagesForAuthorizedAccount(Pageable pageable);

    Mono<Void> deleteMessagesOlderNumberOfDays(Integer days);

}
