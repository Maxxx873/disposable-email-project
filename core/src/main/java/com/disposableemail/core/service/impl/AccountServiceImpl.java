package com.disposableemail.core.service.impl;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.dao.repository.AccountRepository;
import com.disposableemail.core.event.Event;
import com.disposableemail.core.event.producer.EventProducer;
import com.disposableemail.core.exception.custom.AccountAlreadyRegisteredException;
import com.disposableemail.core.exception.custom.AccountNotFoundException;
import com.disposableemail.core.exception.custom.DomainNotAvailableException;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.api.DomainService;
import com.disposableemail.core.util.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.disposableemail.core.dao.entity.AccountEntity.createAccountEntityFromCredentials;
import static com.disposableemail.core.event.Event.Type.AUTH_DELETING_ACCOUNT;
import static com.disposableemail.core.event.Event.Type.MAIL_DELETING_ACCOUNT;
import static com.disposableemail.core.security.SecurityUtils.getCredentialsFromJwt;
import static reactor.function.TupleUtils.function;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    @Value("${mail-server.defaultUserSizeQuota}")
    private String quotaSize;

    private final EventProducer eventProducer;
    private final AccountRepository accountRepository;
    private final DomainService domainService;
    private final TextEncryptor encryptor;

    @Override
    public Mono<AccountEntity> createAccount(Credentials credentials) {
        log.info("Creating an Account | ({})", credentials.getAddress());

        var domain = EmailUtils.getDomainFromEmailAddress(credentials.getAddress());

        return domainService.getByDomain(domain)
                .flatMap(domainEntity -> {
                    log.info("Using a Domain: {}", domainEntity.getDomain());
                    if (isAvailable(domainEntity)) {
                        return accountRepository.findByAddress(credentials.getAddress().toLowerCase())
                                .flatMap(accountEntity -> Mono.error(AccountAlreadyRegisteredException::new))
                                .then(accountRepository.save(createAccountEntityFromCredentials(credentials, quotaSize)));
                    } else {
                        return Mono.just(new AccountEntity());
                    }
                })
                .switchIfEmpty(Mono.error(DomainNotAvailableException::new));
    }

    @Override
    public Mono<AccountEntity> createAccount(AccountEntity accountEntity) {
        log.info("Creating an Account | ({})", accountEntity.getAddress());

        var domain = EmailUtils.getDomainFromEmailAddress(accountEntity.getAddress());

        return domainService.getByDomain(domain)
                .flatMap(domainEntity -> {
                    log.info("Using a Domain: {}", domainEntity.getDomain());
                    if (isAvailable(domainEntity)) {
                        return accountRepository.findByAddress(accountEntity.getAddress().toLowerCase())
                                .flatMap(account -> Mono.error(AccountAlreadyRegisteredException::new))
                                .then(accountRepository.save(accountEntity));
                    } else {
                        return Mono.just(new AccountEntity());
                    }
                })
                .switchIfEmpty(Mono.error(DomainNotAvailableException::new));
    }

    @Override
    public Mono<AccountEntity> getAccountById(String id) {
        log.info("Getting an Account by id {}", id);

        return accountRepository.findById(id);
    }

    @Override
    public Mono<AccountEntity> getAccountByAddress(String address) {
        log.info("Getting an Account by address {}", address);

        return accountRepository.findByAddress(address);
    }

    @Override
    public Mono<AccountEntity> deleteAccount(String id) {
        log.info("Deleting an Account {}", id);

        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .flatMap(accountEntity ->
                        accountRepository.delete(accountEntity).then(Mono.just(accountEntity))
                                .doOnSuccess(action -> {
                                    log.info("Account {} is deleted", id);
                                    eventProducer.send(new Event<>(AUTH_DELETING_ACCOUNT, accountEntity.getAddress()));
                                    eventProducer.send(new Event<>(MAIL_DELETING_ACCOUNT, accountEntity.getAddress()));
                                }));
    }

    @Override
    public Mono<AccountEntity> softDeleteAccount(String id) {
        log.info("Soft deleting an Account {}", id);

        var sIdMono = getCredentialsFromJwt();
        var accountEntityMono = accountRepository.findById(id);
        var result = accountEntityMono.zipWith(sIdMono)
                .flatMap(function((accountEntity, userCredentials) -> {
                    log.info("Get authorized Account | address: {}, subject: {}",
                            userCredentials.getPreferredUsername(), userCredentials.getSub());
                    if (!userCredentials.getPreferredUsername().equals(accountEntity.getAddress())) {
                        return Mono.error(new AccessDeniedException("Account unauthorized"));
                    }
                    accountEntity.setIsDeleted(true);
                    return accountRepository.save(accountEntity)
                            .doOnSuccess(action -> {
                                eventProducer.send(new Event<>(AUTH_DELETING_ACCOUNT, accountEntity.getAddress()));
                                eventProducer.send(new Event<>(MAIL_DELETING_ACCOUNT, accountEntity.getAddress()));
                            });
                }));
        result.subscribe();
        return result;
    }

    @Override
    public Flux<AccountEntity> getAccounts(int size, int offset) {
        log.info("Getting an Accounts | limit: {}, offset: {}", size, offset);

        return accountRepository.findByIdNotNullOrderByCreatedAtDesc().skip(offset).take(size);
    }

    private Credentials getEncryptCredentials(Credentials credentials) {
        return Credentials.builder()
                .address(credentials.getAddress())
                .password(encryptor.encrypt(credentials.getPassword()))
                .build();
    }

    private boolean isAvailable(DomainEntity domainEntity) {
        return Boolean.TRUE.equals(domainEntity.getIsActive()) &&
                Boolean.FALSE.equals(domainEntity.getIsPrivate());
    }
}
