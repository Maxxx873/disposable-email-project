package com.disposableemail.rest.delegate;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.disposableemail.AbstractSpringControllerIntegrationTest;
import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.model.Account;
import com.disposableemail.core.model.Credentials;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

class AccountsApiDelegateImplTest extends AbstractSpringControllerIntegrationTest {

    private final AccountEntity testAccount = AccountEntity.builder()
            .id("1")
            .address("account1@example.com")
            .quota(40000)
            .used(4000)
            .mailboxId("mailboxId1")
            .isDeleted(false)
            .isDisabled(true)
            .build();

    @Test
    void shouldCreateAccountItemWithRateLimiter() {

        var limitForPeriod = rateLimiterRegistry.getAllRateLimiters()
                .stream().toList().get(0).getRateLimiterConfig().getLimitForPeriod();
        Map<Integer, Integer> responseStatusCount = new ConcurrentHashMap<>();

        when(accountService.createAccount(any())).thenReturn(Mono.just(new AccountEntity()));

        IntStream.rangeClosed(1, limitForPeriod + 1)
                .parallel()
                .forEach(i -> {
                    var responseStatus = webTestClient.post()
                            .uri("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .bodyValue(new Credentials("testusername@example.com", "password"))
                            .exchange()
                            .returnResult(String.class)
                            .getStatus()
                            .value();

                    responseStatusCount.merge(responseStatus, 1, Integer::sum);
                });

        assertThat(responseStatusCount)
                .hasSize(2)
                .containsKeys(TOO_MANY_REQUESTS.value(), ACCEPTED.value());

    }

    @Test
    @WithMockJwtAuth(authorities = { "account1@example.com", "ROLE_USER" },
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldReturnAccountItem() {
        when(accountService.getAccountById(testAccount.getId())).thenReturn(Mono.just(testAccount));

        webTestClient.get()
                .uri("/api/v1/accounts/{id}", testAccount.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Account.class)
                .isEqualTo(accountMapper.accountEntityToAccount(testAccount));
    }

    @Test
    @WithMockJwtAuth(authorities = { "account1@example.com", "ROLE_USER" },
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldDeleteAccountItem() {
        when(accountService.softDeleteAccount(testAccount.getId())).thenReturn(Mono.just(testAccount));

        webTestClient.delete()
                .uri("/api/v1/accounts/{id}",testAccount.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    @WithMockJwtAuth(authorities = { "account1@example.com", "ROLE_USER" },
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void testDeleteAccountItemNotFound() {
        when(accountService.softDeleteAccount(testAccount.getId())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/accounts/{id}", testAccount.getId())
                .exchange()
                .expectStatus().isNotFound();
    }


}
