package com.disposableemail.core.service.api;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.model.Token;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface AccountService {

    Mono<AccountEntity> createAccount(Credentials credentials);

    Mono<Token> getTokenFromAuthorizationService(Credentials credentials);

    Mono<AccountEntity> getAccountFromJwt(ServerWebExchange exchange);

    Mono<AccountEntity> getAccountWithUsedSize(ServerWebExchange address);

    Mono<AccountEntity> getAccountById(String id);

    Mono<AccountEntity> setMailboxId(Credentials credentials);

    Mono<AccountEntity> getAccountByAddress(String address);

    Mono<AccountEntity> deleteAccount(String id);
}