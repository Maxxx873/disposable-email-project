package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.DomainEntity;
import com.disposableemail.exception.MailboxNotFoundException;
import com.disposableemail.service.api.MailServerClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ApacheJamesClientServiceImplUnitTest {

    private static final String MAILBOXES_JSON = """
                        [
                            {"mailboxName":"INBOX", "mailboxId": "031f2760-3753-11ed-b0b7-b9c0a2f33564"},
                            {"mailboxName":"Sent", "mailboxId": "413df240-3755-11ed-b0b7-b9c0a2f33564"}
                        ]
            """;

    private static final String DOMAINS_JSON = """
                        [
                        "example.com",
                        "example.org"
                        ]
            """;

    private final int DOMAINS_COUNT = 2;

    private static final String USERNAME = "username@test.com";

    @MockBean
    private RetryRegistry registry;
    @Mock
    private WebClient webClientMock;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock
    private WebClient.ResponseSpec responseMock;

    private MailServerClientService mailServerClientService;

    @BeforeEach
    void setUp() {
        mailServerClientService = new ApacheJamesClientServiceImpl(new ObjectMapper(), webClientMock, registry);
    }

    @Test
    void shouldReturnInboxIdByNameAndUsernameIfExist() {

        var mailboxName = "INBOX";
        var expectedMailBoxId = "031f2760-3753-11ed-b0b7-b9c0a2f33564";

        when(webClientMock.get()).thenReturn(requestHeadersUriMock);
        when(requestHeadersUriMock.uri("/users/{username}/mailboxes", USERNAME)).thenReturn(requestHeadersMock);
        when(requestHeadersMock.retrieve()).thenReturn(responseMock);
        when(responseMock.bodyToMono(String.class)).thenReturn(Mono.just(MAILBOXES_JSON));

        Mono<String> resultMailboxId = mailServerClientService.getMailboxId(USERNAME, mailboxName);

        StepVerifier.create(resultMailboxId).expectNext(expectedMailBoxId).expectComplete().verify();

    }

    @Test
    void shouldThrowMailBoxNotFoundExceptionIfMailBoxNameNotExist() {

        var mailboxName = "INBOX1";

        when(webClientMock.get()).thenReturn(requestHeadersUriMock);
        when(requestHeadersUriMock.uri("/users/{username}/mailboxes", USERNAME)).thenReturn(requestHeadersMock);
        when(requestHeadersMock.retrieve()).thenReturn(responseMock);
        when(responseMock.bodyToMono(String.class)).thenReturn(Mono.just(MAILBOXES_JSON));

        Mono<String> resultMailboxId = mailServerClientService.getMailboxId(USERNAME, mailboxName);

        StepVerifier.create(resultMailboxId).expectErrorMatches(throwable ->
                throwable instanceof MailboxNotFoundException && throwable.getMessage().equals("Mailbox not found"));

    }

    @Test
    void shouldReturnListOfDomains() {

        when(webClientMock.get()).thenReturn(requestHeadersUriMock);
        when(requestHeadersUriMock.uri("/domains")).thenReturn(requestHeadersMock);
        when(requestHeadersMock.retrieve()).thenReturn(responseMock);
        when(responseMock.bodyToMono(String.class)).thenReturn(Mono.just(DOMAINS_JSON));

        Flux<DomainEntity> resultDomainsList = mailServerClientService.getDomains();

        StepVerifier.create(resultDomainsList).expectNextCount(DOMAINS_COUNT).expectComplete().verify();

    }

}