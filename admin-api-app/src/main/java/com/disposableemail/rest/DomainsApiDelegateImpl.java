package com.disposableemail.rest;

import com.disposableemail.api.DomainsApiDelegate;
import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.dao.mapper.DomainMapper;
import com.disposableemail.core.exception.custom.DomainNotAvailableException;
import com.disposableemail.core.model.Domain;
import com.disposableemail.core.model.DomainItem;
import com.disposableemail.core.service.api.DomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
//@PreAuthorize("hasRole(@environment.getProperty('spring.security.role.admin'))")
public class DomainsApiDelegateImpl implements DomainsApiDelegate {

    private final DomainMapper domainMapper;
    private final DomainService domainService;

    @Override
    public Mono<ResponseEntity<Domain>> createDomainItem(Mono<DomainItem> domainItem, ServerWebExchange exchange) {

        return domainItem.flatMap(domainService::createDomain)
                .map(domainEntity -> {
                    log.info("Saved Domain: {}", domainEntity.getDomain());
                    return ResponseEntity.status(HttpStatus.ACCEPTED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(domainMapper.domainEntityToDomain(domainEntity));
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteDomainItem(String id, ServerWebExchange exchange) {
        return domainService.deleteDomain(id)
                .map(value -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    @Override
    public Mono<ResponseEntity<Flux<Domain>>> getDomainCollection(Integer size, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(getDomainCollection(size).map(domainMapper::domainEntityToDomain)));
    }

    @Override
    public Mono<ResponseEntity<Domain>> getDomainItem(String id, ServerWebExchange exchange) {
        return domainService.getDomain(id)
                .map(domainEntity -> {
                    log.info("Retrieved Domain: {}", domainEntity.toString());
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(domainMapper.domainEntityToDomain(domainEntity));
                })
                .switchIfEmpty(Mono.error(new DomainNotAvailableException()));
    }

    private Flux<DomainEntity> getDomainCollection(Integer size) {
        if (!Objects.equals(size, null)) {
            return domainService.getDomainsExcludingLocalhost(size);
        }
        return domainService.getAllDomains();
    }

}
