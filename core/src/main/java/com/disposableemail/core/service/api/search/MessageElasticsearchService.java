package com.disposableemail.core.service.api.search;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.dao.entity.search.MessageElasticsearchEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageElasticsearchService {

    Flux<MessageElasticsearchEntity> getMessagesFromMailbox(AccountEntity accountEntity);

    Flux<MessageElasticsearchEntity> getMessagesFromMailbox(Pageable pageable, ServerWebExchange exchange);

    Mono<MessageElasticsearchEntity> getMessage(String messageId, ServerWebExchange exchange);

    Mono<Void> deleteMessageById(String messageId);

    Mono<MessageElasticsearchEntity> softDeleteMessageById(String messageId);

}
