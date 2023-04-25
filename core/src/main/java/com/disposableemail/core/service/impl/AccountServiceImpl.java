package com.disposableemail.core.service.impl;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.dao.repository.AccountRepository;
import com.disposableemail.core.dao.repository.DomainRepository;
import com.disposableemail.core.event.Event;
import com.disposableemail.core.event.EventProducer;
import com.disposableemail.core.exception.custom.AccountAlreadyRegisteredException;
import com.disposableemail.core.exception.custom.AccountNotFoundException;
import com.disposableemail.core.exception.custom.DomainNotAvailableException;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.model.Token;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.api.auth.AuthorizationService;
import com.disposableemail.core.service.api.mail.MailServerClientService;
import com.disposableemail.core.util.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.IDToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.disposableemail.core.event.Event.Type.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    @Value("${mail-server.mailbox}")
    private String inbox;

    @Value("${mail-server.defaultUserSizeQuota}")
    private String quotaSize;

    private final EventProducer eventProducer;
    private final AccountRepository accountRepository;
    private final AuthorizationService authorizationService;
    private final DomainRepository domainRepository;
    private final MailServerClientService mailServerClientService;
    private static final String PREFERRED_USERNAME = "preferred_username";
    private final TextEncryptor encryptor;

    @Override
    public Mono<AccountEntity> createAccount(Credentials credentials) {
        log.info("Creating an Account | ({})", credentials.getAddress());

        return domainRepository.findByDomain(EmailUtils.getDomainFromEmailAddress(credentials.getAddress()))
                .doOnError(throwable -> log.error("Trying to find a Domain", throwable))
                .map(domainEntity -> {
                    log.info("Using a Domain: {}", domainEntity.toString());
                    if (Boolean.TRUE.equals(domainEntity.getIsActive()) &&
                            Boolean.FALSE.equals(domainEntity.getIsPrivate())) {
                        eventProducer.send(new Event<>(START_CREATING_ACCOUNT, getEncryptCredentials(credentials)));
                        return accountRepository.findByAddress(credentials.getAddress().toLowerCase())
                                .flatMap(accountEntity -> Mono.error(AccountAlreadyRegisteredException::new))
                                .then(accountRepository.save(getNewAccountEntity(credentials)));
                    } else {
                        return Mono.just(new AccountEntity());
                    }
                }).flatMap(accountEntity -> accountEntity)
                .switchIfEmpty(Mono.error(DomainNotAvailableException::new));
    }

    @Override
    public Mono<Token> getTokenFromAuthorizationService(Credentials credentials) {
        log.info("Getting a Token for an Account | ({})", credentials.getAddress());

        return Mono.just(authorizationService.getToken(credentials));
    }

    @Override
    public Mono<AccountEntity> getAccountFromJwt(ServerWebExchange exchange) {
        log.info("Getting an Account from | {}", PREFERRED_USERNAME);

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getPrincipal())
                .cast(Jwt.class)
                .map(jwt -> {
                    log.info("Retrieved User name {} from JWT", jwt.getClaimAsString(PREFERRED_USERNAME));
                    return jwt.getClaimAsString(PREFERRED_USERNAME);
                })
                .flatMap(accountRepository::findByAddress);
    }

    //TODO get userID
    public Mono<String> getUserIdFromJwt() {
        log.info("Getting an User ID from JWT");

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getPrincipal())
                .map(principal -> {
                    String userIdfromJwt = "";
                    if (principal instanceof KeycloakPrincipal) {
                        KeycloakPrincipal<KeycloakSecurityContext> kPrincipal = (KeycloakPrincipal<KeycloakSecurityContext>) principal;
                        IDToken token = kPrincipal.getKeycloakSecurityContext().getIdToken();
                        userIdfromJwt = token.getSubject();
                        log.info("Received User ID from JWT | {}", userIdfromJwt);
                    }
                    return userIdfromJwt;
                });
    }


    @Override
    public Mono<AccountEntity> getAccountWithUsedSize(ServerWebExchange exchange) {
        log.info("Getting an Account with used size | {}", PREFERRED_USERNAME);

        var accountEntity = this.getAccountFromJwt(exchange);
        var usedSize = accountEntity.flatMap(account -> mailServerClientService.getUsedSize(account.getAddress()));
        var result = accountEntity
                .zipWith(usedSize)
                .map(tuple2 -> {
                    log.info("Get used size for Account | address: {}, used size: {}",
                            tuple2.getT1().getAddress(), tuple2.getT2());
                    var account = tuple2.getT1();
                    account.setUsed(tuple2.getT2());
                    return account;
                })
                .flatMap(accountRepository::save);
        result.subscribe();
        return result;
    }

    @Override
    public Mono<AccountEntity> getAccountById(String id) {
        log.info("Getting an Account by id {}", id);

        return accountRepository.findById(id);
    }

    @Override
    public Mono<AccountEntity> setMailboxId(Credentials credentials) {
        var result = mailServerClientService.getMailboxId(credentials, inbox)
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
    public Mono<AccountEntity> softDeleteAccount(String id) {
        log.info("Soft deleting an Account {}}", id);

        return accountRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .flatMap(accountEntity -> {
                    log.info("Account {} is soft deleted", id);
                    accountEntity.setIsDeleted(true);
              //      eventProducer.send(new Event<>(START_DELETING_ACCOUNT, id));
                    return accountRepository.save(accountEntity).then(Mono.just(accountEntity));
                });
    }

    private AccountEntity getNewAccountEntity(Credentials credentials) {
        return AccountEntity.builder()
                .address(credentials.getAddress())
                .isDeleted(false)
                .isDisabled(false)
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
