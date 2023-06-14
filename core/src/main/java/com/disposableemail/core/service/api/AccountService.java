package com.disposableemail.core.service.api;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.model.Credentials;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Mono<AccountEntity> createAccount(Credentials credentials);

    Mono<AccountEntity> getAccountById(String id);

    Mono<AccountEntity> getAccountByAddress(String address);

    Mono<AccountEntity> deleteAccount(String id);

    Mono<AccountEntity> softDeleteAccount(String id);

    Flux<AccountEntity> getAccounts(int size, int offset);
}
