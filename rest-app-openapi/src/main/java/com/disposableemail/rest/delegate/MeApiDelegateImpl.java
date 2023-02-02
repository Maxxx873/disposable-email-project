package com.disposableemail.rest.delegate;

import com.disposableemail.dao.mapper.AccountMapper;
import com.disposableemail.exception.AccountNotFoundException;
import com.disposableemail.rest.api.MeApiDelegate;
import com.disposableemail.rest.model.Account;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class MeApiDelegateImpl implements MeApiDelegate {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Account>> getMeAccountItem(ServerWebExchange exchange) {

        return accountService.getAccountWithUsedSize(exchange)
                .map(accountMapper::accountEntityToAccount)
                .map(account -> {
                    log.info("Extracted authenticated Account: {}", account.toString());
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(account);
                })
                .switchIfEmpty(Mono.error(new AccountNotFoundException()));
    }
}
