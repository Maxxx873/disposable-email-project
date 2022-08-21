package com.disposableemail.rest;

import com.disposableemail.dao.mapper.MessageMapper;
import com.disposableemail.exception.MessageNotFoundException;
import com.disposableemail.rest.api.MessagesApiDelegate;
import com.disposableemail.rest.model.Message;
import com.disposableemail.rest.model.Messages;
import com.disposableemail.service.api.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagesApiDelegateImpl implements MessagesApiDelegate {

    private final MessageMapper messageMapper;
    private final MessageService messageService;

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Flux<Messages>>> getMessageCollection(Integer size, ServerWebExchange exchange) {

        return Mono.just(ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(messageService.getMessages(exchange).map(messageMapper::messageEntityToMessages)));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Message>> getMessageItem(String id, ServerWebExchange exchange) {

        return messageService.getMessage(id)
                .map(messageEntity -> {
                    log.info("Retrieved Account: {}", messageEntity.toString());
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(messageMapper.messageEntityToMessage(messageEntity));
                })
                .switchIfEmpty(Mono.error(new MessageNotFoundException()));
    }
}
