package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.DomainEntity;
import com.disposableemail.exception.MailboxNotFoundException;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.service.api.MailServerClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.ws.rs.core.Response;

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
    private static final String PASSWORD = "password";

    @MockBean
    private RetryRegistry registry;
    @Mock
    private ObjectMapper mapper;
    @Mock
    private ObjectNode node;
    @Mock
    private WebClient webClientMock;
    @Mock
    private WebClient.RequestHeadersSpec requestHeaders;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUri;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec response;

    private MailServerClientService mailServerClientService;

    @BeforeEach
    void setUp() {
        mailServerClientService = new ApacheJamesClientServiceImpl(new ObjectMapper(), webClientMock, registry);
    }

    @Test
    void shouldReturnInboxIdByNameAndUsernameIfExist() {

        var mailboxName = "INBOX";
        var expectedMailBoxId = "031f2760-3753-11ed-b0b7-b9c0a2f33564";

        when(webClientMock.get()).thenReturn(requestHeadersUri);
        when(requestHeadersUri.uri("/users/{username}/mailboxes", USERNAME)).thenReturn(requestHeaders);
        when(requestHeaders.retrieve()).thenReturn(response);
        when(response.bodyToMono(String.class)).thenReturn(Mono.just(MAILBOXES_JSON));

        Mono<String> resultMailboxId = mailServerClientService.getMailboxId(USERNAME, mailboxName);

        StepVerifier.create(resultMailboxId).expectNext(expectedMailBoxId).expectComplete().verify();

    }

    @Test
    void shouldThrowMailBoxNotFoundExceptionIfMailBoxNameNotExist() {

        var mailboxName = "INBOX1";

        when(webClientMock.get()).thenReturn((requestHeadersUri));
        when(requestHeadersUri.uri("/users/{username}/mailboxes", USERNAME)).thenReturn(requestHeaders);
        when(requestHeaders.retrieve()).thenReturn(response);
        when(response.bodyToMono(String.class)).thenReturn(Mono.just(MAILBOXES_JSON));

        Mono<String> resultMailboxId = mailServerClientService.getMailboxId(USERNAME, mailboxName);

        StepVerifier.create(resultMailboxId).expectErrorMatches(throwable ->
                throwable instanceof MailboxNotFoundException && throwable.getMessage().equals("Mailbox not found"));

    }

    @Test
    void shouldCreateUser() throws JsonProcessingException {

        var credentials = new Credentials(USERNAME, PASSWORD);
        var requestBody = String.format("{\"password\":\"%s\"}", PASSWORD);
        var uri = String.format("/users/%s", USERNAME);

        when(webClientMock.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(mapper.writeValueAsString(node)).thenReturn(requestBody);
        when(requestBodySpec.bodyValue(mapper.writeValueAsString(node))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(response);
        when(response.bodyToMono(Response.class)).thenReturn(Mono.empty());

        Mono<Response> response = mailServerClientService.createUser(credentials);

        StepVerifier.create(response).expectComplete().verify();

    }

    @Test
    void shouldReturnListOfDomains() {

        when(webClientMock.get()).thenReturn(requestHeadersUri);
        when(requestHeadersUri.uri("/domains")).thenReturn(requestHeaders);
        when(requestHeaders.retrieve()).thenReturn(response);
        when(response.bodyToMono(String.class)).thenReturn(Mono.just(DOMAINS_JSON));

        Flux<DomainEntity> resultDomainsList = mailServerClientService.getDomains();

        StepVerifier.create(resultDomainsList).expectNextCount(DOMAINS_COUNT).expectComplete().verify();

    }

}