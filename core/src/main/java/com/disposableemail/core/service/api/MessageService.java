package com.disposableemail.core.service.api;

import com.disposableemail.core.dao.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageService {
    Mono<MessageEntity> getMessage(String messageId, ServerWebExchange exchange);

    Mono<MessageEntity> saveMessage(MessageEntity messageEntity);

    Mono<MessageEntity> getMessageById(String messageId);

    Mono<MessageEntity> softDeleteMessage(String messageId, ServerWebExchange exchange);

    Mono<MessageEntity> updateMessage(String messageId, MessageEntity messageEntity,  ServerWebExchange exchange);

    Flux<MessageEntity> getMessages(Integer size, ServerWebExchange exchange);

    Flux<MessageEntity> getMessagesByAccountId(Pageable pageable, ServerWebExchange exchange);

}
