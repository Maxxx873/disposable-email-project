package com.disposableemail.service.api;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface AccountService {

    Mono<AccountEntity> createAccount(Credentials credentials);

    Mono<Token> getTokenFromAuthorizationService(Credentials credentials);

    Mono<AccountEntity> getAccountFromJwt(ServerWebExchange exchange);

    String getDomainFromEmailAddress(String address);

    Mono<AccountEntity> getAccountById(String id);

    Mono<AccountEntity> setMailboxId(Credentials credentials);

    Mono<AccountEntity> getAccountByAddress(String address);

    Mono<AccountEntity> deleteAccount(String id);
}
