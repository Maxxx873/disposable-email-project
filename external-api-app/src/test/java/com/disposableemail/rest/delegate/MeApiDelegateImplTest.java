package com.disposableemail.rest.delegate;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.disposableemail.AbstractSpringControllerIntegrationTest;
import com.disposableemail.core.model.Account;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class MeApiDelegateImplTest extends AbstractSpringControllerIntegrationTest {

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldGetMeAccountItem() {
        when(accountHelperService.getAuthorizedAccountWithUsedSize()).thenReturn(Mono.just(testAccountEntity));

        webTestClient.get().uri("/api/v1/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Account.class)
                .isEqualTo(testAccount);

        verify(accountHelperService, times(1)).getAuthorizedAccountWithUsedSize();
    }

    @Test
    void shouldNotGetMeAccountItemWithUnauthorizedAccount() {
        when(accountHelperService.getAuthorizedAccountWithUsedSize()).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v1/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldGetMeAccountItemWithError() {
        when(accountHelperService.getAuthorizedAccountWithUsedSize()).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v1/me")
                .exchange()
                .expectStatus().isNotFound();

        verify(accountHelperService, times(1)).getAuthorizedAccountWithUsedSize();
    }
}