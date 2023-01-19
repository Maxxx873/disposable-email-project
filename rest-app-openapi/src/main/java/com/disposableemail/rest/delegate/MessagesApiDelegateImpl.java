package com.disposableemail.rest.delegate;

import com.disposableemail.dao.mapper.MessageMapper;
import com.disposableemail.dao.mapper.search.MessageElasticsearchMapper;
import com.disposableemail.exception.MessageNotFoundException;
import com.disposableemail.exception.MessagesNotFoundException;
import com.disposableemail.rest.api.MessagesApiDelegate;
import com.disposableemail.rest.model.Message;
import com.disposableemail.rest.model.Messages;
import com.disposableemail.service.api.MessageService;
import com.disposableemail.service.api.search.MessageElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagesApiDelegateImpl implements MessagesApiDelegate {

    private final MessageMapper messageMapper;
    private final MessageService messageService;
    private final MessageElasticsearchService messageElasticsearchService;
    private final MessageElasticsearchMapper messageElasticsearchMapper;

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Flux<Messages>>> getMessageCollection(Integer page, Integer size, ServerWebExchange exchange) {

        return Mono.just(ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(messageElasticsearchService.getMessagesFromMailbox(
                                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date")), exchange)
                                .map(messageElasticsearchMapper::messageElasticsearchEntityToMessages)))
                .switchIfEmpty(Mono.error(new MessagesNotFoundException()));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Void>> deleteMessageItem(String id, ServerWebExchange exchange) {

        return messageService.deleteMessage(id, exchange)
                .switchIfEmpty(Mono.error(new MessageNotFoundException()))
                .map(value -> ResponseEntity.noContent().build());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Message>> patchMessageItem(String id, Mono<Message> message, ServerWebExchange exchange) {

        return messageMapper.messageToMessageEntity(message).flatMap(messageEntity ->
                        messageService.updateMessage(id, messageEntity, exchange))
                .switchIfEmpty(Mono.error(new MessageNotFoundException()))
                .map(messageEntity -> ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(messageMapper.messageEntityToMessage(messageEntity)));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Message>> getMessageItem(String id, ServerWebExchange exchange) {

        return messageService.getMessage(id, exchange)
                .map(messageEntity -> {
                    log.info("Retrieved Message | message: {}", messageEntity.toString());
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(messageMapper.messageEntityToMessage(messageEntity));
                })
                .switchIfEmpty(Mono.error(new MessageNotFoundException()));
    }

}
