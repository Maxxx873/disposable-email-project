package com.disposableemail.core.service.impl;

import com.disposableemail.AbstractSpringIntegrationTest;
import com.disposableemail.core.dao.entity.AccountEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountServiceImplTest extends AbstractSpringIntegrationTest {
    private final AccountEntity account1 = AccountEntity.builder()
            .id("1")
            .address("account1@email.com")
            .quota(40000)
            .used(4000)
            .mailboxId("mailboxId1")
            .isDeleted(false)
            .isDisabled(true)
            .build();
    private final AccountEntity account2 = AccountEntity.builder()
            .id("2")
            .address("account2@email.com")
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
    }

    @AfterEach
    public void cleanUp() {
        accountRepository.deleteAll().block();
    }

    @Test
    void shouldFindAllAccounts() {
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
        accountService.deleteAccount("1").block();
        var accounts = accountService.getAccounts(2, 0);

        StepVerifier
                .create(accounts)
                .expectSubscription()
                .assertNext(account -> {
                    assertEquals(account2.getId(), account.getId());
                    assertEquals(account2.getAddress(), account.getAddress());
                    assertEquals(account2.getIsDeleted(), account.getIsDeleted());
                    assertEquals(account2.getQuota(), account.getQuota());
                    assertEquals(account2.getUsed(), account.getUsed());
                    assertEquals(account2.getMailboxId(), account.getMailboxId());
                    assertEquals(account2.getIsDisabled(), account.getIsDisabled());
                })
                .expectComplete()
                .verify();
    }
}