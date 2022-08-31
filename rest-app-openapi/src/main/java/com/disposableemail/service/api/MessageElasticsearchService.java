package com.disposableemail.service.api;

import com.disposableemail.dao.entity.MessageElasticsearchEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageElasticsearchService {
    Flux<MessageElasticsearchEntity> getMessages(ServerWebExchange exchange);

    Mono<MessageElasticsearchEntity> getMessage(String id, ServerWebExchange exchange);
}
