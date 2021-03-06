package com.disposableemail.service.api;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface AccountService {

    Mono<AccountEntity> createAccountInAuthorizationServiceAndSaveToDb(Credentials credentials);

    Mono<Token> getTokenFromAuthorizationService(Credentials credentials);

    Mono<AccountEntity> getAccountFromAddress (String address);
}
