package com.disposableemail.rest;


import com.disposableemail.dao.mapper.AccountMapper;
import com.disposableemail.dao.repository.AccountRepository;
import com.disposableemail.rest.api.AccountsApiDelegate;
import com.disposableemail.rest.model.Account;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.service.api.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.ws.rs.NotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountsApiDelegateImpl implements AccountsApiDelegate {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Account>> deleteAccountItem(String id, ServerWebExchange exchange) {

        return accountRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException()))
                .flatMap(accountEntity -> {
                    log.info("Deleted Account Id: {}", id);
                    return accountRepository.delete(accountEntity).then(Mono.just(accountEntity));
                })
                .map(accountMapper::accountEntityToAccount)
                .map(account -> ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(account));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Account>> getAccountItem(String id, ServerWebExchange exchange) {

        return accountRepository.findById(id)
                .map(accountEntity -> {
                    log.info("Retrieved Account: {}", accountEntity.toString());
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(accountMapper.accountEntityToAccount(accountEntity));
                })
                .switchIfEmpty(Mono.error(new NotFoundException()));
    }

    @Override
    public Mono<ResponseEntity<Account>> createAccountItem(Mono<Credentials> credentials, ServerWebExchange exchange) {

        return credentials.flatMap(accountService::createAccountInAuthorizationServiceAndSaveToDb).map(accountEntity -> {
            log.info("Saved Account: {}", accountEntity.toString());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(accountMapper.accountEntityToAccount(accountEntity));
        });
    }
}
