package com.disposableemail.service.api.search;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.dao.entity.search.MessageElasticsearchEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageElasticsearchService {
    Flux<MessageElasticsearchEntity> getMessages(ServerWebExchange exchange);

    Flux<MessageElasticsearchEntity> getMessagesFromMailbox(ServerWebExchange exchange);

    Flux<MessageElasticsearchEntity> getMessagesFromMailbox(AccountEntity accountEntity);

    Flux<MessageElasticsearchEntity> getMessagesFromMailbox(Integer size, ServerWebExchange exchange);

    Mono<MessageElasticsearchEntity> getMessage(String id, ServerWebExchange exchange);

}
