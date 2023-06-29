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

        var accountEntity = getCredentialsFromJwt()
                .switchIfEmpty(Mono.error(new AccessDeniedException("Access denied")))
                .map(UserCredentials::getPreferredUsername)
                .flatMap(accountService::getAccountByAddress);
        var usedSize = accountEntity.flatMap(account -> mailServerClientService.getUsedSize(account.getAddress()));
        var result = accountEntity.zipWith(usedSize)
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

    public Mono<AccountEntity> setMailboxId(Credentials credentials) {
        log.info("Setting a mailbox id for an Account {} | ", credentials.getAddress());

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

}
