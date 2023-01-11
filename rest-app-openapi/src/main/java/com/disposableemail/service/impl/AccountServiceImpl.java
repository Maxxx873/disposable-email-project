package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.dao.repository.AccountRepository;
import com.disposableemail.dao.repository.DomainRepository;
import com.disposableemail.event.EventProducer;
import com.disposableemail.exception.AccountNotFoundException;
import com.disposableemail.exception.DomainNotAvailableException;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;
import com.disposableemail.service.api.AccountService;
import com.disposableemail.service.api.auth.AuthorizationService;
import com.disposableemail.service.api.mail.MailServerClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    @Value("${mail-server.mailbox}")
    private String INBOX;

    @Value("${mail-server.defaultUserSizeQuota}")
    private String quotaSize;

    private final EventProducer domainEventProducer;
    private final AccountRepository accountRepository;
    private final AuthorizationService authorizationService;
    private final DomainRepository domainRepository;
    private final MailServerClientService mailServerClientService;
    private final String USER_NAME_CLAIM = "preferred_username";
    private final TextEncryptor encryptor;

    @Override
    public Mono<AccountEntity> createAccount(Credentials credentials) {
        log.info("Creating an Account | ({})", credentials.getAddress());

        return domainRepository.findByDomain(getDomainFromEmailAddress(credentials.getAddress()))
                .doOnError(throwable -> log.error("Trying to find a Domain", throwable))
                .map(domainEntity -> {
                    log.info("Using a Domain: {}", domainEntity.toString());
                    if (domainEntity.getIsActive() && !domainEntity.getIsPrivate()) {
                        domainEventProducer.sendStartCreatingAccount(getEncryptCredentials(credentials));
                        return accountRepository.save(getNewAccountEntity(credentials));
                    } else {
                        return Mono.just(new AccountEntity());
                    }
                }).flatMap(accountEntity -> accountEntity)
                .switchIfEmpty(Mono.error(new DomainNotAvailableException()));
    }

    @Override
    public Mono<Token> getTokenFromAuthorizationService(Credentials credentials) {
        log.info("Getting a Token for an Account | ({})", credentials.getAddress());

        return Mono.just(authorizationService.getToken(credentials));
    }

    @Override
    public Mono<AccountEntity> getAccountFromJwt(ServerWebExchange exchange) {
        log.info("Getting an Account from {}", USER_NAME_CLAIM);

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getPrincipal())
                .cast(Jwt.class)
                .map(jwt -> {
                    log.info("Retrieved User name {} from JWT", jwt.getClaimAsString(USER_NAME_CLAIM));
                    return jwt.getClaimAsString(USER_NAME_CLAIM);
                }).flatMap(accountRepository::findByAddress);
    }

    @Override
    public Mono<AccountEntity> getAccountById(String id) {
        log.info("Getting an Account by id {}", id);

        return accountRepository.findById(id);
    }

    @Async
    @Override
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "account-quota-size-updated", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "account-quota-size-updated"),
            key = "quota-size-updated-in-mail-service"
    ))
    public Mono<AccountEntity> setMailboxId(Credentials credentials) {
        var result = mailServerClientService.getMailboxId(credentials, INBOX)
                .zipWith(accountRepository.findByAddress(credentials.getAddress()))
                .flatMap(tuple2 -> {
                    log.info("Set Mailbox id for Account | address: {}, mailbox: {}",
                            credentials.getAddress(), tuple2.getT1());
                    var accountEntity = tuple2.getT2();
                    accountEntity.setMailboxId(tuple2.getT1());
                    return accountRepository.save(accountEntity);
                });
        result.subscribe();
        return result;
    }

    @Override
    public Mono<AccountEntity> getAccountByAddress(String address) {
        log.info("Getting an Account by address {}}", address);

        return accountRepository.findByAddress(address);
    }

    @Override
    public Mono<AccountEntity> deleteAccount(String id) {
        log.info("Deleting an Account {}}", id);

        return accountRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .flatMap(accountEntity -> {
                    log.info("Account {} is deleted", id);
                    return accountRepository.delete(accountEntity).then(Mono.just(accountEntity));
                });
    }

    @Override
    public String getDomainFromEmailAddress(String address) {
        return address.substring(address.indexOf("@") + 1);
    }

    private AccountEntity getNewAccountEntity(Credentials credentials) {
        return AccountEntity.builder()
                .address(credentials.getAddress())
                .isDeleted(false)
                .isDeleted(false)
                .used(0)
                .quota(Integer.parseInt(quotaSize))
                .build();
    }

    private Credentials getEncryptCredentials(Credentials credentials) {
        return Credentials.builder()
                .address(credentials.getAddress())
                .password(encryptor.encrypt(credentials.getPassword()))
                .build();
    }
}
