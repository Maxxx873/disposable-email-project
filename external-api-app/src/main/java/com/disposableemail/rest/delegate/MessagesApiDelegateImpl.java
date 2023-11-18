package com.disposableemail.rest.delegate;

import com.disposableemail.api.MessagesApiDelegate;
import com.disposableemail.core.dao.mapper.MessageMapper;
import com.disposableemail.core.exception.custom.MessageNotFoundException;
import com.disposableemail.core.exception.custom.MessagesNotFoundException;
import com.disposableemail.core.model.Message;
import com.disposableemail.core.model.Messages;
import com.disposableemail.core.service.api.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
@PreAuthorize("hasRole(@environment.getProperty('spring.security.role.user'))")
public class MessagesApiDelegateImpl implements MessagesApiDelegate {

    private final MessageMapper messageMapper;
    private final MessageService messageService;

    @Override
    public Mono<ResponseEntity<Flux<Messages>>> getMessageCollection(Integer page, Integer size, ServerWebExchange exchange) {

        return Mono.just(ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(messageService.getMessagesForAuthorizedAccount(PageRequest.of(page, size))
                                .map(messageMapper::messageEntityToMessages)))
                .switchIfEmpty(Mono.error(new MessagesNotFoundException()));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteMessageItem(String id, ServerWebExchange exchange) {

        return messageService.softDeleteMessage(id)
                .switchIfEmpty(Mono.error(new MessageNotFoundException()))
                .map(value -> ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<Message>> patchMessageItem(String id, Mono<Message> message, ServerWebExchange exchange) {

        return messageMapper.messageToMessageEntity(message).flatMap(messageEntity ->
                        messageService.updateMessage(id, messageEntity))
                .switchIfEmpty(Mono.error(new MessageNotFoundException()))
                .map(messageEntity -> ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(messageMapper.messageEntityToMessage(messageEntity)));
    }

    @Override
    public Mono<ResponseEntity<Message>> getMessageItem(String id, ServerWebExchange exchange) {

        return messageService.getMessage(id)
                .map(messageEntity -> {
                    log.info("Retrieved Message | message: {}", messageEntity.toString());
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(messageMapper.messageEntityToMessage(messageEntity));
                })
                .switchIfEmpty(Mono.error(new MessageNotFoundException()));
    }
}
