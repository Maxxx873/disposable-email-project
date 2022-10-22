package com.disposableemail.rest.delegate;

import com.disposableemail.dao.mapper.SourceMapper;
import com.disposableemail.exception.SourceNotFoundException;
import com.disposableemail.rest.api.SourcesApiDelegate;
import com.disposableemail.rest.model.Source;
import com.disposableemail.service.api.MessageService;
import com.disposableemail.service.api.SourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceApiDelegateImpl implements SourcesApiDelegate {

    private final SourceMapper sourceMapper;
    private final SourceService sourceService;
    private final MessageService messageService;

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Source>> getSourceItem(String id, ServerWebExchange exchange) {
        return messageService.getMessage(id, exchange)
                .flatMap(messageEntity -> {
                    log.info("Retrieved Message: {}", messageEntity.toString());
                    return sourceService.getSourceByMsgId(messageEntity.getMsgid());
                })
                .map(sourceEntity -> {
                    log.info("Retrieved Source: {}", sourceEntity.toString());
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(sourceMapper.sourceEntityToSource(sourceEntity));
                }).switchIfEmpty(Mono.error(new SourceNotFoundException()));
    }


}
