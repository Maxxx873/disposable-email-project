package com.disposableemail.service.api;

import com.disposableemail.dao.entity.MessageEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageService {
    Flux<MessageEntity> getMessages(ServerWebExchange exchange);

    Mono<MessageEntity> getMessage(String id, ServerWebExchange exchange);
}
