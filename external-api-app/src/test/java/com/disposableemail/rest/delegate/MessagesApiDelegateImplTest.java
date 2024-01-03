package com.disposableemail.rest.delegate;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.disposableemail.AbstractSpringControllerIntegrationTest;
import com.disposableemail.core.model.Message;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class MessagesApiDelegateImplTest extends AbstractSpringControllerIntegrationTest {

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldReturnMessageCollection() {
        int page = 1;
        int size = 1;
        when(messageService.getMessagesForAuthorizedAccount(PageRequest.of(page, size))).thenReturn(Flux.just(testMessageEntity));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/messages")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Message.class)
                .hasSize(size);

        verify(messageService, times(1)).getMessagesForAuthorizedAccount(PageRequest.of(page, size));
    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldThrowExceptionWhenMessageCollectionNotFound() {
        int page = 1;
        int size = 1;
        when(messageService.getMessagesForAuthorizedAccount(PageRequest.of(page, size))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/messages")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);

        verify(messageService, times(1)).getMessagesForAuthorizedAccount(PageRequest.of(page, size));
    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldDeleteMessageItem() {
        when(messageService.softDeleteMessage(testMessageEntity.getId())).thenReturn(Mono.just(testMessageEntity));

        webTestClient.delete()
                .uri("/api/v1/messages/{id}", testMessageEntity.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(messageService, times(1)).softDeleteMessage(any());
    }

    @Test
    void shouldDeleteMessageItemThrowExceptionIfNotAuthorized() {

        webTestClient.delete()
                .uri("/api/v1/messages/{id}", testMessageEntity.getId())
                .exchange()
                .expectStatus().isUnauthorized();

        verify(messageService, times(0)).softDeleteMessage(any());

    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldUpdateMessageItem() {
        when(messageService.updateMessage(any(), any())).thenReturn(Mono.just(testMessageEntity));

        webTestClient.patch()
                .uri("/api/v1/messages/{id}", testMessageEntity.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testMessageEntity)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Message.class)
                .isEqualTo(messageMapper.messageEntityToMessage(testMessageEntity));

        verify(messageService, times(1)).updateMessage(any(), any());

    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldThrowExceptionWhenUpdateMessageItemNotFound() {
        when(messageService.updateMessage(any(), any())).thenReturn(Mono.empty());

        webTestClient.patch()
                .uri("/api/v1/messages/{id}", testMessageEntity.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(testMessageEntity)
                .exchange()
                .expectStatus().isNotFound();

        verify(messageService, times(1)).updateMessage(any(), any());
    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldReturnMessageItem() {
        when(messageService.getMessage(testMessageEntity.getId())).thenReturn(Mono.just(testMessageEntity));

        webTestClient.get()
                .uri("/api/v1/messages/{id}", testMessageEntity.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Message.class)
                .isEqualTo(messageMapper.messageEntityToMessage(testMessageEntity));

        verify(messageService, times(1)).getMessage(any());
    }

    @Test
    void shouldThrowExceptionWhenGetMessageItemUnauthorized() {

        webTestClient.get()
                .uri("/api/v1/messages/{id}", testMessageEntity.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();

        verify(messageService, times(0)).getMessage(any());
    }

    @Test
    @WithMockJwtAuth(authorities = {"account1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "account1@example.com"))
    void shouldThrowExceptionWhenMessageNotFound() {
        when(messageService.getMessage(testMessageEntity.getId())).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/messages/{id}", testMessageEntity.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(messageService, times(1)).getMessage(any());
    }
}