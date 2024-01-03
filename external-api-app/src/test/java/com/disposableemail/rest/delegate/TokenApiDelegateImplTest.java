package com.disposableemail.rest.delegate;

import com.disposableemail.AbstractSpringControllerIntegrationTest;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.model.Token;
import jakarta.ws.rs.NotAuthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class TokenApiDelegateImplTest extends AbstractSpringControllerIntegrationTest {

    @Test
    void shouldReturnToken() {
        var credentials = new Credentials("testusername@example.com", "password");
        var token = Mono.just(new Token("token"));
        when(accountHelperService.getTokenFromAuthorizationService(credentials)).thenReturn(token);

        webTestClient.post()
                .uri("/api/v1/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .exchange()
                .expectStatus().isOk();

        verify(accountHelperService, times(1)).getTokenFromAuthorizationService(credentials);
    }

    @Test
    void shouldThrowExceptionIfCredentialsAreIncorrect() {
        var credentials = new Credentials("testusername@example.com", "password");
        when(accountHelperService.getTokenFromAuthorizationService(credentials)).thenThrow(NotAuthorizedException.class);

        webTestClient.post()
                .uri("/api/v1/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .exchange()
                .expectStatus().isUnauthorized();

        verify(accountHelperService, times(1)).getTokenFromAuthorizationService(credentials);
    }
}