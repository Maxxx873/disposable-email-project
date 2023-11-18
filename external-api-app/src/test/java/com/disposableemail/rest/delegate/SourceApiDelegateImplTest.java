package com.disposableemail.rest.delegate;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.disposableemail.AbstractSpringControllerIntegrationTest;
import com.disposableemail.core.model.Source;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class SourceApiDelegateImplTest extends AbstractSpringControllerIntegrationTest {

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldGetSourceItem() {
        var messageId = testMessageEntity.getId();
        var msgId = testMessageEntity.getMsgid();
        when(messageService.getMessage(messageId)).thenReturn(Mono.just(testMessageEntity));
        when(sourceService.getSourceByMsgId(msgId)).thenReturn(Mono.just(testSourceEntity));

        webTestClient.get()
                .uri("/api/v1/sources/{id}", messageId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Source.class)
                .isEqualTo(testSource);

        verify(messageService, times(1)).getMessage(messageId);
        verify(sourceService, times(1)).getSourceByMsgId(msgId);
    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldGetMeSourceItemWithError() {
        var messageId = testMessageEntity.getId();
        when(messageService.getMessage(messageId)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/sources/{id}", messageId)
                .exchange()
                .expectStatus().isNotFound();

        verify(messageService, times(1)).getMessage(messageId);
        verify(sourceService, never()).getSourceByMsgId(anyString());
    }

    @Test
    void shouldNotGetMeSourceItemWithUnauthorizedAccount() {
        var messageId = testMessageEntity.getId();
        webTestClient.get()
                .uri("/api/v1/sources/{id}", messageId)
                .exchange()
                .expectStatus().isUnauthorized();

    }

}