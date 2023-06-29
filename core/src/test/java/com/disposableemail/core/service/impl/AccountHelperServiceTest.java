package com.disposableemail.core.service.impl;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.disposableemail.AbstractSpringIntegrationTest;
import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.model.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AccountHelperServiceTest extends AbstractSpringIntegrationTest {

    private final Credentials credentials = Credentials.builder()
            .address("account@example.com")
            .password("password")
            .build();

    private final AccountEntity account = AccountEntity.builder()
            .address("account@example.com")
            .build();

    @BeforeEach
    public void setUp() {
        accountRepository.save(account).block();
    }

    @AfterEach
    public void cleanUp() {
        accountRepository.deleteAll().block();
    }

    @Test
    void shouldGetTokenFromAuthorizationService() {
        var tokenValue = "token";
        when(authorizationService.getToken(credentials)).thenReturn(new Token(tokenValue));

        StepVerifier
                .create(accountHelperService.getTokenFromAuthorizationService(credentials))
                .expectSubscription()
                .assertNext(token -> assertEquals(token.getToken(), tokenValue))
                .expectComplete()
                .verify();
    }

    @Test
    @WithMockJwtAuth(authorities = {"accoun@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account@example.com"))
    void shouldGetAuthorizedAccountWithUsedSize() {
        int newUsedSize = 5000;
        when(mailServerClientService.getUsedSize(account.getAddress())).thenReturn(Mono.just(newUsedSize));

        accountHelperService.getAuthorizedAccountWithUsedSize().block();

        StepVerifier
                .create(accountService.getAccountById(account.getId()))
                .expectSubscription()
                .assertNext(acc -> assertEquals(acc.getUsed(), newUsedSize))
                .expectComplete()
                .verify();
    }

    @Test
    void shouldThrowExceptionWhenIfNotAuthorizedUser() {

        StepVerifier
                .create(accountHelperService.getAuthorizedAccountWithUsedSize())
                .expectSubscription()
                .expectErrorMatches(throwable -> throwable instanceof AccessDeniedException)
                .verify();
    }

    @Test
    void shouldSetMailboxId() {
        var mailboxId = "mailboxId";
        when(mailServerClientService.getMailboxId(credentials, inbox)).thenReturn(Mono.just(mailboxId));

        accountHelperService.setMailboxId(credentials).block();

        StepVerifier
                .create(accountService.getAccountById(account.getId()))
                .expectSubscription()
                .assertNext(acc -> assertEquals(acc.getMailboxId(), mailboxId))
                .expectComplete()
                .verify();
    }

}