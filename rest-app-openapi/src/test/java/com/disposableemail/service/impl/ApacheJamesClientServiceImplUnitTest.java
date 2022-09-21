package com.disposableemail.service.impl;

import com.disposableemail.exception.MailboxNotFoundException;
import com.disposableemail.service.api.MailServerClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
class ApacheJamesClientServiceImplUnitTest {

    private static final String MAILBOXES_JSON = """
            [
                {"mailboxName":"INBOX", "mailboxId": "031f2760-3753-11ed-b0b7-b9c0a2f33564"},
                {"mailboxName":"Sent", "mailboxId": "413df240-3755-11ed-b0b7-b9c0a2f33564"}
            ]
                                       """;

    private static final String BASE_URL = "http://localhost:8000/";

    private static final String USERNAME = "username@test.com";

    WebClient webClientMock = WebClient.builder().baseUrl(BASE_URL)
            .exchangeFunction(clientRequest -> Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("content-type", "application/json")
                    .body(MAILBOXES_JSON)
                    .build()))
            .build();

    @MockBean
    private RetryRegistry registry;

    private final MailServerClientService mailServerClientService = new ApacheJamesClientServiceImpl(new ObjectMapper(), webClientMock, registry);

    @Test
    void shouldReturnInboxIdByNameAndUsernameIfExist() {

        var mailboxName = "INBOX";
        var expectedMailBoxId = "031f2760-3753-11ed-b0b7-b9c0a2f33564";

        when(webClientMock.get().uri(uriBuilder -> uriBuilder
                        .path("/users/{username}/mailboxes")
                        .build(USERNAME))
                .retrieve()
                .bodyToMono(String.class))
                .thenReturn(MAILBOXES_JSON);

        Mono<String> resultMailboxId = mailServerClientService.getMailboxId(USERNAME, mailboxName);

        StepVerifier.create(resultMailboxId).expectNext(expectedMailBoxId).expectComplete().verify();

    }

    @Test
    void shouldThrowMailBoxNotFoundExceptionIfMAi() {

        var mailboxName = "INBOX1";

        when(webClientMock.get().uri(uriBuilder -> uriBuilder
                        .path("/users/{username}/mailboxes")
                        .build(USERNAME))
                .retrieve()
                .bodyToMono(String.class))
                .thenReturn(MAILBOXES_JSON);

        Mono<String> resultMailboxId = mailServerClientService.getMailboxId(USERNAME, mailboxName);

        StepVerifier.create(resultMailboxId).expectErrorMatches(throwable ->
                throwable instanceof MailboxNotFoundException && throwable.getMessage().equals("Mailbox not found"));

    }

}