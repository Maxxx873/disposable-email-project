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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AuthorizationService authorizationService;
    private final CredentialsMapper credentialsMapper;
    private final AccountMapper accountMapper;

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
}
