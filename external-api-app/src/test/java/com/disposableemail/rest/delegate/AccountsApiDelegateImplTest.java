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
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

class AccountsApiDelegateImplTest extends AbstractSpringControllerIntegrationTest {

    @Test
    void shouldCreateAccountItemWithRateLimiter() {

        var limitForPeriod = rateLimiterRegistry.getAllRateLimiters()
                .stream().toList().get(0).getRateLimiterConfig().getLimitForPeriod();
        Map<Integer, Integer> responseStatusCount = new ConcurrentHashMap<>();
        when(accountFacade.createAccount(any())).thenReturn(Mono.just(new AccountEntity()));

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

        verify(accountFacade, times(limitForPeriod)).createAccount(any());

    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldReturnAccountItem() {
        when(accountService.getAccountById(testAccountEntity.getId())).thenReturn(Mono.just(testAccountEntity));

        webTestClient.get()
                .uri("/api/v1/accounts/{id}", testAccountEntity.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Account.class)
                .isEqualTo(testAccount);

        verify(accountService, times(1)).getAccountById(testAccountEntity.getId());
    }

    @Test
    void shouldThrowExceptionWhenGetAccountUnauthorized() {

        webTestClient.get()
                .uri("/api/v1/accounts/{id}", testAccountEntity.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();

        verify(accountService, times(0)).getAccountById(any());
    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldThrowExceptionWhenAccountNotFound() {
        when(accountService.getAccountById(testAccountEntity.getId())).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/accounts/{id}", testAccountEntity.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(accountService, times(1)).getAccountById(any());
    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldDeleteAccountItem() {
        when(accountService.softDeleteAccount(testAccountEntity.getId())).thenReturn(Mono.just(testAccountEntity));

        webTestClient.delete()
                .uri("/api/v1/accounts/{id}", testAccountEntity.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(accountService, times(1)).softDeleteAccount(any());
    }

    @Test
    void shouldDeleteAccountItemThrowExceptionIfNotAuthorized() {

        webTestClient.delete()
                .uri("/api/v1/accounts/{id}", testAccountEntity.getId())
                .exchange()
                .expectStatus().isUnauthorized();

        verify(accountService, times(0)).softDeleteAccount(any());
    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void testDeleteAccountItemNotFound() {
        when(accountService.softDeleteAccount(testAccountEntity.getId())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/accounts/{id}", testAccountEntity.getId())
                .exchange()
                .expectStatus().isNotFound();

        verify(accountService, times(1)).softDeleteAccount(any());
    }

}
