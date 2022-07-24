package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.dao.mapper.AccountMapper;
import com.disposableemail.dao.mapper.CredentialsMapper;
import com.disposableemail.dao.repository.AccountRepository;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;
import com.disposableemail.service.api.AccountService;
import com.disposableemail.service.api.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AuthorizationService authorizationService;
    private final CredentialsMapper credentialsMapper;
    private final AccountMapper accountMapper;

    private final String USER_NAME_CLAIM = "preferred_username";

    @Override
    public Mono<AccountEntity> createAccountInAuthorizationServiceAndSaveToDb(Credentials credentials) {
        log.info("Creating Account | ({})", credentials.getAddress());
        authorizationService.createUser(credentials);
        var account = credentialsMapper.credentialsToAccount(credentials);
        return accountRepository.save(accountMapper.accountToAccountEntity(account));
    }

    @Override
    public Mono<Token> getTokenFromAuthorizationService(Credentials credentials) {
        log.info("Getting token for an Account | ({})", credentials.getAddress());
        return Mono.just(authorizationService.getToken(credentials));
    }

    @Override
    public Mono<AccountEntity> getAccountFromJwt(ServerWebExchange exchange) {
        log.info("Getting user name from {}}", USER_NAME_CLAIM);
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getPrincipal())
                .cast(Jwt.class)
                .map(jwt -> jwt.getClaimAsString(USER_NAME_CLAIM)).flatMap(accountRepository::findByAddress);
    }
}
