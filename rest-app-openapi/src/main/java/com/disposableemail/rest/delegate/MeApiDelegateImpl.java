package com.disposableemail.rest.delegate;

import com.disposableemail.dao.mapper.AccountMapper;
import com.disposableemail.exception.AccountNotFoundException;
import com.disposableemail.rest.api.MeApiDelegate;
import com.disposableemail.rest.model.Account;
import com.disposableemail.service.api.AccountService;
import com.disposableemail.service.api.mail.MailServerClientService;
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
    private final MailServerClientService mailServerClientService;

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Account>> getMeAccountItem(ServerWebExchange exchange) {

        var accountEntity = accountService.getAccountFromJwt(exchange);
        var usedSize = accountEntity.flatMap(account -> mailServerClientService.getUsedSize(account.getAddress()));
        return accountEntity
                .map(accountMapper::accountEntityToAccount)
                .zipWith(usedSize)
                .map(tuple2 -> {
                    log.info("Get used size for Account | address: {}, used size: {}",
                            tuple2.getT1().getAddress(), tuple2.getT2());
                    var account = tuple2.getT1();
                    account.setUsed(tuple2.getT2());
                    return account;
                })
                .map(account -> {
                    log.info("Extracting authenticated Account: {}", account.toString());
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(account);
                })
                .switchIfEmpty(Mono.error(new AccountNotFoundException()));
    }
}
