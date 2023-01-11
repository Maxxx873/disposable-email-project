package com.disposableemail.service.impl.mail;

import com.disposableemail.dao.entity.DomainEntity;
import com.disposableemail.event.EventProducer;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.service.api.mail.MailServerClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApacheJamesClientServiceImpl implements MailServerClientService {

    @Value("${mail-server.name}")
    private String mailServerName;
    @Value("${mail-server.mailbox}")
    private String INBOX;

    @Value("${mail-server.defaultUserSizeQuota}")
    private String quotaSize;

    private final String MAILBOX_ID_KEY = "mailboxId";
    private final String PASSWORD_FIELD = "password";
    private final ObjectMapper mapper;
    private final WebClient mailServerApiClient;
    private final RetryRegistry registry;
    private final EventProducer eventProducer;

    private final TextEncryptor encryptor;


    @PostConstruct
    public void postConstruct() {
        registry.retry("retryMailService").getEventPublisher().onRetry(ev ->
                log.info("Connect to {} API: {}", mailServerName, ev));
    }

    @Override
    @Retry(name = "retryMailService")
    public Mono<String> getMailboxId(Credentials credentials, String mailboxName) {

        var result = mailServerApiClient.get().uri("/users/{username}/mailboxes", credentials.getAddress())
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(reactor.util.retry.Retry.fixedDelay(5, Duration.ofSeconds(2)))
                .map(response -> {
                    try {
                        log.info("Getting mailboxes for user {} | {}", credentials.getAddress(), response);
                        return mapper.readValue(response, new TypeReference<List<Map<String, String>>>() {
                        });
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .map(mailboxes ->
                        mailboxes.stream().filter(mailbox ->
                                        mailbox.containsValue(mailboxName) && mailbox.containsKey(MAILBOX_ID_KEY))
                                .findFirst())
                .map(optional -> {
                    var mailboxId = optional.stream().iterator().next().get(MAILBOX_ID_KEY);
                    log.info("Extracted mailboxId | {}", mailboxId);
                    return mailboxId;
                });
        result.subscribe();
        return result;
    }

    @Override
    @Retry(name = "retryMailService")
    public Flux<DomainEntity> getDomains() {

        return mailServerApiClient.get().uri("/domains").retrieve().bodyToMono(String.class).map(response -> {
            try {
                log.info("Getting domains from {} | {}", mailServerName, response);
                return mapper.readValue(response, new TypeReference<List<String>>() {
                });
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }).flatMapIterable(domains -> domains).map(domainName -> DomainEntity.builder()
                .domain(domainName)
                .isActive(true)
                .isPrivate(false)
                .build());
    }

    @Async
    @Override
    @SneakyThrows
    @Retry(name = "retryMailService")
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "account-keycloak-confirmation", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "account-keycloak-confirmation"),
            key = "keycloak-confirmation-account"
    ))
    public Mono<Response> createUser(Credentials credentials) {
        log.info("Creating a User in Mail Server {} | User: {}", mailServerName, credentials.getAddress());

        var password = mapper.createObjectNode();
        password.put(PASSWORD_FIELD, encryptor.decrypt(credentials.getPassword()));
        var result = mailServerApiClient.put()
                .uri("/users/" + credentials.getAddress())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(password))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        log.info("User created in Mail Service | Status code: {} | Address: {}",
                                response.statusCode(), credentials.getAddress());
                        eventProducer.sendAccountCreatedInMailService(credentials);
                        return response.bodyToMono(Response.class);
                    }
                    return Mono.empty();
                }).retryWhen(reactor.util.retry.Retry.fixedDelay(3, Duration.ofSeconds(2)));
        result.subscribe();
        return result;
    }

    @Async
    @Override
    @SneakyThrows
    @Retry(name = "retryMailService")
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "account-created-in-mail-service", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "account-created-in-mail-service"),
            key = "account-created-in-mail-service"
    ))
    public Mono<Response> createMailbox(Credentials credentials) {
        log.info("Creating a Mailbox in Mail Server {} | User: {}, Mailbox: {}",
                mailServerName, credentials.getAddress(), INBOX);

        var result = mailServerApiClient.put()
                .uri("/users/" + credentials.getAddress() + "/mailboxes/" + INBOX)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        log.info("Mailbox created in Mail Service | Status code: {} | Address: {}",
                                response.statusCode(), credentials.getAddress());
                        eventProducer.sendMailboxCreatedInMailService(credentials);
                        return response.bodyToMono(Response.class);
                    }
                    return Mono.empty();
                }).retryWhen(reactor.util.retry.Retry.fixedDelay(3, Duration.ofSeconds(2)));
        result.subscribe();
        return result;
    }

    @Override
    public Mono<Integer> getQuotaSize(String username) {
        log.info("Getting the quota size for a user | User: {}", username);

        var result = mailServerApiClient.get()
                .uri("/quota/users/" + username + "/size")
                .retrieve()
                .bodyToMono(Integer.class)
                .retryWhen(reactor.util.retry.Retry.fixedDelay(3, Duration.ofSeconds(2)));
        result.subscribe();
        return result;
    }

    @Async
    @Override
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "account-mailbox-created", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "account-mailbox-created"),
            key = "mailbox-created-in-mail-service"
    ))
    public Mono<Response> updateQuotaSize(Credentials credentials) {
        log.info("Updating the quota size for a user | User: {}", credentials.getAddress());

        var result = mailServerApiClient.put()
                .uri("/quota/users/" + credentials.getAddress() + "/size")
                .bodyValue(Integer.parseInt(quotaSize))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        log.info("Quota size updated in Mail Service | Status code: {} | Address: {}",
                                response.statusCode(), credentials.getAddress());
                        eventProducer.sendQuotaSizeUpdatedInMailService(credentials);
                        return response.bodyToMono(Response.class);
                    }
                    return Mono.empty();
                }).retryWhen(reactor.util.retry.Retry.fixedDelay(3, Duration.ofSeconds(2)));
        result.subscribe();
        return result;
    }

    @Override
    public Mono<Integer> getUsedSize(String username) {
        log.info("Getting used size for a user | User: {}", username);

        var result = mailServerApiClient.get()
                .uri("/quota/users/" + username)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(reactor.util.retry.Retry.fixedDelay(3, Duration.ofSeconds(2)))
                .map(jsonString -> {
                    try {
                        var quota = mapper.readTree(jsonString);
                        var occupation = quota.get("occupation");
                        return occupation.get("size").asInt();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
        result.subscribe();
        return result;
    }

}
