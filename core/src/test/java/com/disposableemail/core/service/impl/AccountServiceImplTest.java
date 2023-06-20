package com.disposableemail.core.service.impl;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.disposableemail.AbstractSpringIntegrationTest;
import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.model.Credentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountServiceImplTest extends AbstractSpringIntegrationTest {

    private final DomainEntity domain = new DomainEntity("1", "example.com", true, false);

    private final AccountEntity account1 = AccountEntity.builder()
            .id("1")
            .address("account1@example.com")
            .quota(40000)
            .used(4000)
            .mailboxId("mailboxId1")
            .isDeleted(false)
            .isDisabled(true)
            .build();
    private final AccountEntity account2 = AccountEntity.builder()
            .id("2")
            .address("account2@example.com")
            .quota(50000)
            .used(5000)
            .mailboxId("mailboxId2")
            .isDeleted(true)
            .isDisabled(true)
            .build();

    @BeforeEach
    public void setUp() {
        accountRepository.save(account1).block();
        accountRepository.save(account2).block();
        domainRepository.save(domain).block();
    }

    @AfterEach
    public void cleanUp() {
        accountRepository.deleteAll().block();
        domainRepository.deleteAll().block();
    }

    @Test
    void shouldCreateAccount() {
        int size = 3;
        var credentials = new Credentials("account3@example.com", "password");

        accountService.createAccount(credentials).block();

        StepVerifier
                .create(accountRepository.findAll())
                .expectSubscription()
                .expectNextCount(size)
                .expectComplete()
                .verify();
    }

    @Test
    void shouldGetAccountById() {

        var account = accountService.getAccountById("1");

        StepVerifier
                .create(account)
                .expectSubscription()
                .assertNext(acc -> {
                    assertEquals(account1.getId(), acc.getId());
                    assertEquals(account1.getAddress(), acc.getAddress());
                    assertEquals(account1.getIsDeleted(), acc.getIsDeleted());
                    assertEquals(account1.getQuota(), acc.getQuota());
                    assertEquals(account1.getUsed(), acc.getUsed());
                    assertEquals(account1.getMailboxId(), acc.getMailboxId());
                    assertEquals(account1.getIsDisabled(), acc.getIsDisabled());
                })
                .expectComplete()
                .verify();
    }

    @Test
    void shouldGetAccountByAddress() {

        var account = accountService.getAccountByAddress("account1@example.com");

        StepVerifier
                .create(account)
                .expectSubscription()
                .assertNext(acc -> {
                    assertEquals(account1.getId(), acc.getId());
                    assertEquals(account1.getAddress(), acc.getAddress());
                    assertEquals(account1.getIsDeleted(), acc.getIsDeleted());
                    assertEquals(account1.getQuota(), acc.getQuota());
                    assertEquals(account1.getUsed(), acc.getUsed());
                    assertEquals(account1.getMailboxId(), acc.getMailboxId());
                    assertEquals(account1.getIsDisabled(), acc.getIsDisabled());
                })
                .expectComplete()
                .verify();
    }


    @Test
    void shouldGetAccounts() {
        int size = 2;

        var accounts = accountService.getAccounts(size, 0);

        StepVerifier
                .create(accounts)
                .expectSubscription()
                .expectNextCount(size)
                .expectComplete()
                .verify();
    }

    @Test
    void shouldDeleteAccount() {
        int size = 2;

        accountService.deleteAccount("1").block();

        var accounts = accountService.getAccounts(size, 0);

        StepVerifier
                .create(accounts)
                .expectSubscription()
                .expectNextCount(size - 1)
                .expectComplete()
                .verify();
    }

    @Test
    @WithMockJwtAuth(authorities = { "account1@example.com", "ROLE_USER" },
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldSoftDeleteAccount() {

        var account = accountService.softDeleteAccount(account1.getId());

        StepVerifier
                .create(account)
                .expectSubscription()
                .assertNext(acc -> {
                    assertEquals(account1.getId(), acc.getId());
                    assertEquals(account1.getAddress(), acc.getAddress());
                    assertEquals(true, acc.getIsDeleted());
                    assertEquals(account1.getQuota(), acc.getQuota());
                    assertEquals(account1.getUsed(), acc.getUsed());
                    assertEquals(account1.getMailboxId(), acc.getMailboxId());
                    assertEquals(account1.getIsDisabled(), acc.getIsDisabled());
                })
                .expectComplete()
                .verify();
    }

    @Test
    @WithMockJwtAuth(authorities = { "account1@example.com", "ROLE_USER" },
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldThrowExceptionIfAuthorizedAnotherUserSoftDeleteAccount() {

        var account = accountService.softDeleteAccount(account2.getId());

        StepVerifier
                .create(account)
                .expectSubscription()
                .expectErrorMatches(throwable -> throwable instanceof AccessDeniedException)
                .verify();
    }

}
