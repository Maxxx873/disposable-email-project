package com.disposableemail.service.api;

import com.disposableemail.dao.entity.MessageEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageService {
    Mono<MessageEntity> getMessage(String messageId, ServerWebExchange exchange);

    Mono<MessageEntity> saveMessage(MessageEntity messageEntity);

    Mono<MessageEntity> getMessageById(String messageId);

    Mono<MessageEntity> deleteMessage(String messageId, ServerWebExchange exchange);

    Mono<MessageEntity> updateMessage(String messageId, MessageEntity messageEntity,  ServerWebExchange exchange);

    Flux<MessageEntity> getMessages(Integer size, ServerWebExchange exchange);
}
