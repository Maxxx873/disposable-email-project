package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.DomainEntity;
import com.disposableemail.event.EventProducer;
import com.disposableemail.exception.MailboxNotFoundException;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.service.api.mail.MailServerClientService;
import com.disposableemail.service.impl.mail.ApacheJamesClientServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.ws.rs.core.Response;
import java.util.function.Function;

import static org.mockito.Mockito.when;

@Slf4j
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

    private static final String QUOTA_JSON = """
                        {
                            "global": null,
                            "domain": null,
                            "user": {
                                "count": null,
                                "size": 40001
                            },
                            "computed": {
                                "count": null,
                                "size": 40001
                            },
                            "occupation": {
                                "size": 33640,
                                "count": 8,
                                "ratio": {
                                    "size": 0.8409789755256118,
                                    "count": 0.0,
                                    "max": 0.8409789755256118
                                }
                            }
                        }
            """;


    private static final String USERNAME = "username@test.com";
    private static final String PASSWORD = "password";

    @MockBean
    private RetryRegistry registry;
    @Mock
    private EventProducer eventProducer;
    @Mock
    private TextEncryptor encryptor;
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
        mailServerClientService = new ApacheJamesClientServiceImpl(new ObjectMapper(), webClientMock, registry, eventProducer, encryptor);
        ReflectionTestUtils.setField(mailServerClientService, "quotaPath", "/quota/users/");
        ReflectionTestUtils.setField(mailServerClientService, "quotaSize", "40000");
        ReflectionTestUtils.setField(mailServerClientService, "inbox", "INBOX");
    }

    @Test
    void shouldReturnInboxIdByNameAndUsernameIfExist() {

        var mailboxName = "INBOX";
        var expectedMailBoxId = "031f2760-3753-11ed-b0b7-b9c0a2f33564";
        var credentials = new Credentials(USERNAME, PASSWORD);

        when(webClientMock.get()).thenReturn(requestHeadersUri);
        when(requestHeadersUri.uri("/users/{username}/mailboxes", credentials.getAddress())).thenReturn(requestHeaders);
        when(requestHeaders.retrieve()).thenReturn(response);
        when(response.bodyToMono(String.class)).thenReturn(Mono.just(MAILBOXES_JSON));

        Mono<String> resultMailboxId = mailServerClientService.getMailboxId(credentials, mailboxName);

        StepVerifier.create(resultMailboxId).expectNext(expectedMailBoxId).expectComplete().verify();

    }

    @Test
    void shouldThrowMailBoxNotFoundExceptionIfMailBoxNameNotExist() {

        var mailboxName = "INBOX1";
        var credentials = new Credentials(USERNAME, PASSWORD);

        when(webClientMock.get()).thenReturn((requestHeadersUri));
        when(requestHeadersUri.uri("/users/{username}/mailboxes", credentials.getAddress())).thenReturn(requestHeaders);
        when(requestHeaders.retrieve()).thenReturn(response);
        when(response.bodyToMono(String.class)).thenReturn(Mono.just(MAILBOXES_JSON));

        Mono<String> resultMailboxId = mailServerClientService.getMailboxId(credentials, mailboxName);

        StepVerifier.create(resultMailboxId).expectErrorMatches(throwable ->
                throwable instanceof MailboxNotFoundException && throwable.getMessage().equals("Mailbox not found"));

    }

    @Test
    void shouldCreateUser() throws JsonProcessingException {

        var credentials = new Credentials(USERNAME, PASSWORD);
        var requestBody = String.format("{\"password\":\"%s\"}", PASSWORD);
        var uri = String.format("/users/%s", USERNAME);

        when(encryptor.decrypt(credentials.getPassword())).thenReturn(PASSWORD);
        when(webClientMock.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(mapper.writeValueAsString(node)).thenReturn(requestBody);
        when(requestBodySpec.bodyValue(mapper.writeValueAsString(node))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(ArgumentMatchers.<Function<ClientResponse, ? extends Mono<String>>>notNull()))
                .thenReturn(Mono.empty());

        Mono<Response> response = mailServerClientService.createUser(credentials);

        StepVerifier.create(response).expectComplete().verify();

    }

    @Test
    void shouldCreateMailbox() throws JsonProcessingException {

        var mailboxName = "INBOX";
        var credentials = new Credentials(USERNAME, PASSWORD);
        var uri = String.format("/users/%s/mailboxes/%s", USERNAME, mailboxName);

        when(webClientMock.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodySpec);
        when(requestBodySpec.exchangeToMono(ArgumentMatchers.<Function<ClientResponse, ? extends Mono<String>>>notNull()))
                .thenReturn(Mono.empty());

        Mono<Response> response = mailServerClientService.createMailbox(credentials);

        StepVerifier.create(response).expectComplete().verify();

    }

    @Test
    void shouldReturnListOfDomains() {

        var domainsCount = 2;

        when(webClientMock.get()).thenReturn(requestHeadersUri);
        when(requestHeadersUri.uri("/domains")).thenReturn(requestHeaders);
        when(requestHeaders.retrieve()).thenReturn(response);
        when(response.bodyToMono(String.class)).thenReturn(Mono.just(DOMAINS_JSON));

        Flux<DomainEntity> resultDomainsList = mailServerClientService.getDomains();

        StepVerifier.create(resultDomainsList).expectNextCount(domainsCount).expectComplete().verify();

    }

    @Test
    void shouldUpdateQuoteSizeForUSer() {

        var quotaSize = 40000;
        var credentials = new Credentials(USERNAME, PASSWORD);
        var uri = String.format("/quota/users/%s/size", USERNAME);

        when(webClientMock.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(quotaSize)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(ArgumentMatchers.<Function<ClientResponse, ? extends Mono<String>>>notNull()))
                .thenReturn(Mono.empty());

        Mono<Response> response = mailServerClientService.updateQuotaSize(credentials);

        StepVerifier.create(response).expectComplete().verify();

    }

    @Test
    void shouldGetQuoteSizeForUSer() {

        var quotaSize = 40000;
        var uri = String.format("/quota/users/%s/size", USERNAME);

        when(webClientMock.get()).thenReturn(requestHeadersUri);
        when(requestHeadersUri.uri(uri)).thenReturn(requestHeaders);
        when(requestHeaders.retrieve()).thenReturn(response);
        when(response.bodyToMono(Integer.class)).thenReturn(Mono.just(quotaSize));

        Mono<Integer> response = mailServerClientService.getQuotaSize(USERNAME);

        StepVerifier.create(response).expectNext(quotaSize).expectComplete().verify();
    }

    @Test
    void shouldGetUsedSizeForUSer() {

        var expectedUsedSize = 33640;
        var uri = String.format("/quota/users/%s", USERNAME);

        when(webClientMock.get()).thenReturn(requestHeadersUri);
        when(requestHeadersUri.uri(uri)).thenReturn(requestHeaders);
        when(requestHeaders.retrieve()).thenReturn(response);
        when(response.bodyToMono(String.class)).thenReturn(Mono.just(QUOTA_JSON));

        Mono<Integer> usedSize = mailServerClientService.getUsedSize(USERNAME);

        StepVerifier.create(usedSize).expectNext(expectedUsedSize).expectComplete().verify();
    }


}