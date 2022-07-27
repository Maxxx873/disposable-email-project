package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.dao.mapper.AccountMapper;
import com.disposableemail.dao.mapper.CredentialsMapper;
import com.disposableemail.dao.repository.AccountRepository;
import com.disposableemail.dao.repository.DomainRepository;
import com.disposableemail.exception.DomainNotAvailableException;
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
    private final DomainRepository domainRepository;
    private final String USER_NAME_CLAIM = "preferred_username";

    @Override
    public Mono<AccountEntity> createAccountInAuthorizationServiceAndSaveToDb(Credentials credentials) {
        log.info("Creating an Account | ({})", credentials.getAddress());
        return domainRepository.findByDomain(getDomainFromEmailAddress(credentials.getAddress()))
                .doOnError(throwable -> log.error("Trying to find domain", throwable))
                .map(domainEntity -> {
                    log.info("Using a Domain: {}", domainEntity.toString());
                    if (domainEntity.getIsActive() && !domainEntity.getIsPrivate()) {
                        authorizationService.createUser(credentials);
                        var accountEntity = accountMapper
                                .accountToAccountEntity(credentialsMapper.credentialsToAccount(credentials));
                        accountEntity.setDomain(domainEntity);
                        return accountRepository.save(accountEntity);
                    } else {
                        return Mono.just(new AccountEntity());
                    }
                }).flatMap(accountEntity -> accountEntity)
                .switchIfEmpty(Mono.error(new DomainNotAvailableException()));
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

    @Override
    public String getDomainFromEmailAddress(String address) {
        return address.substring(address.indexOf("@") + 1);
    }
}
