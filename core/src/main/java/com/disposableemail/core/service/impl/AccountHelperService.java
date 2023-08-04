package com.disposableemail.core.service.impl;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.dao.repository.AccountRepository;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.model.Token;
import com.disposableemail.core.security.UserCredentials;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.api.auth.AuthorizationService;
import com.disposableemail.core.service.api.mail.MailServerClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.disposableemail.core.security.SecurityUtils.getCredentialsFromJwt;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountHelperService {

    @Value("${mail-server.mailbox}")
    private String inbox;

    private final AuthorizationService authorizationService;
    private final MailServerClientService mailServerClientService;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public Mono<Token> getTokenFromAuthorizationService(Credentials credentials) {
        log.info("Getting a Token for an Account | ({})", credentials.getAddress());

        return Mono.just(authorizationService.getToken(credentials));
    }

    public Mono<AccountEntity> getAuthorizedAccountWithUsedSize() {
        return getCredentialsFromJwt()
                .switchIfEmpty(Mono.error(new AccessDeniedException("Access denied")))
                .map(UserCredentials::getPreferredUsername)
                .flatMap(accountService::getAccountByAddress)
                .flatMap(account -> mailServerClientService.getUsedSize(account.getAddress())
                        .map(usedSize -> {
                            log.info("Get used size for Account | address: {}, used size: {}", account.getAddress(), usedSize);
                            account.setUsed(usedSize);
                            return account;
                        }))
                .flatMap(accountRepository::save)
                .doOnSuccess(result -> log.info("Used size received successfully"))
                .doOnError(err -> log.error("Error getting Used size", err));
    }

    public Mono<AccountEntity> setMailboxId(Credentials credentials) {
        log.info("Setting a mailbox id for an Account {} | ", credentials.getAddress());

        return mailServerClientService.getMailboxId(credentials, inbox)
                .zipWith(accountRepository.findByAddress(credentials.getAddress()))
                .flatMap(tuple -> {
                    var mailboxId = tuple.getT1();
                    var accountEntity = tuple.getT2();
                    log.info("Set Mailbox id for Account | address: {}, mailbox: {}", credentials.getAddress(), mailboxId);
                    accountEntity.setMailboxId(mailboxId);
                    return accountRepository.save(accountEntity);
                })
                .doOnSuccess(result -> log.info("Mailbox id saved successfully"))
                .doOnError(err -> log.error("Error saving mailbox id", err));
    }

}
