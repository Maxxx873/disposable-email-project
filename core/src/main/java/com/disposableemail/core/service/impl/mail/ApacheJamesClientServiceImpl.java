package com.disposableemail.core.service.impl.mail;

import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.event.Event;
import com.disposableemail.core.event.producer.EventProducer;
import com.disposableemail.core.exception.custom.DomainNotAvailableException;
import com.disposableemail.core.exception.custom.MailServerConnectException;
import com.disposableemail.core.exception.custom.MailboxNotFoundException;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.service.api.mail.MailServerClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.disposableemail.core.event.Event.Type;
import static reactor.util.retry.Retry.fixedDelay;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApacheJamesClientServiceImpl implements MailServerClientService {

    @Value("${mail-server.name}")
    private String mailServerName;
    @Value("${mail-server.mailbox}")
    private String inbox;
    @Value("${mail-server.defaultUserSizeQuota}")
    private String quotaSize;
    @Value("${mail-server.quotaPath}")
    private String quotaPath;

    private static final String MAILBOX_ID_KEY = "mailboxId";
    private static final String PASSWORD_FIELD = "password";
    private static final String USERS_PATH = "/users/";
    private static final int MAX_ATTEMPTS = 5;
    private static final int DURATION_SECONDS = 2;

    private final ObjectMapper mapper;
    private final WebClient mailServerApiClient;
    private final RetryRegistry registry;
    private final EventProducer eventProducer;
    private final TextEncryptor encryptor;


    @PostConstruct
    public void postConstruct() {
        registry.retry("retryMailService").getEventPublisher().onRetry(ev ->
                log.warn("Retry connect to {} API: {}", mailServerName, ev));
    }

    @Override
    @Retry(name = "retryMailService")
    public Mono<String> getMailboxId(Credentials credentials, String mailboxName) {

        var result = mailServerApiClient.get().uri("/users/{username}/mailboxes", credentials.getAddress())
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(fixedDelay(MAX_ATTEMPTS, Duration.ofSeconds(DURATION_SECONDS)))
                .map(response -> {
                    try {
                        log.info("Getting mailboxes for user {} | {}", credentials.getAddress(), response);
                        return mapper.readValue(response, new TypeReference<List<Map<String, String>>>() {
                        });
                    } catch (JsonProcessingException ex) {
                        throw new MailboxNotFoundException();
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
                throw new DomainNotAvailableException();
            }
        }).flatMapIterable(domains -> domains).map(domainName -> DomainEntity.builder()
                .domain(domainName)
                .isActive(true)
                .isPrivate(false)
                .build());
    }

    @Override
    @Retry(name = "retryMailService")
    public Mono<Response> createDomain(String domain) {
        log.info("Creating a Domain in Mail Server {} | Domain: {}", mailServerName, domain);

        var result = mailServerApiClient.put()
                .uri("/domains/" + domain)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        log.info("Domain created in Mail Service | Status code: {} | Domain: {}",
                                response.statusCode(), domain);
                        return response.bodyToMono(Response.class);
                    }
                    return Mono.empty();
                }).retryWhen(fixedDelay(MAX_ATTEMPTS, Duration.ofSeconds(DURATION_SECONDS)));
        result.subscribe();
        return result;
    }

    @Override
    @Retry(name = "retryMailService")
    public Mono<Response> deleteDomain(String domain) {
        log.info("Deleting a Domain in Mail Server {} | Domain: {}", mailServerName, domain);

        var result = mailServerApiClient.delete()
                .uri("/domains/" + domain)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        log.info("Deleted Domain in Mail Server | Status code: {} | Domain: {}",
                                response.statusCode(), domain);
                        return response.bodyToMono(Response.class);
                    }
                    return Mono.empty();
                }).retryWhen(fixedDelay(MAX_ATTEMPTS, Duration.ofSeconds(DURATION_SECONDS)));
        result.subscribe();
        return result;
    }

    @Override
    @Retry(name = "retryMailService")
    public Mono<Response> createUser(Credentials credentials) throws JsonProcessingException {
        log.info("Creating a User in Mail Server {} | User: {}", mailServerName, credentials.getAddress());

        var password = mapper.createObjectNode();
        password.put(PASSWORD_FIELD, encryptor.decrypt(credentials.getPassword()));
        return mailServerApiClient.put()
                .uri(USERS_PATH + credentials.getAddress())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(password))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        log.info("User created in Mail Service | Status code: {} | Address: {}",
                                response.statusCode(), credentials.getAddress());
                        eventProducer.send(new Event<>(Type.ACCOUNT_CREATED_IN_MAIL_SERVICE, credentials));
                        return response.bodyToMono(Response.class);
                    }
                    return Mono.empty();
                })
                .onErrorResume(ex -> {
                    log.error("Error creating user on {}: {}", mailServerName, ex.getMessage());
                    return Mono.error(ex);
                })
                .retryWhen(fixedDelay(MAX_ATTEMPTS, Duration.ofSeconds(1))
                        .doAfterRetry(retrySignal -> eventProducer.send(new Event<>(Type.DELETING_ACCOUNT, credentials.getAddress()))))
                .onErrorComplete(throwable -> {
                    throw new MailServerConnectException();
                });
    }

    @Override
    public Mono<Response> deleteUser(String username) {
        log.info("Deleting | User: {}", username);

        var result = mailServerApiClient.delete()
                .uri(USERS_PATH + username)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        log.info("Deleted user in Mail Server | Status code: {} | Address: {}",
                                response.statusCode(), username);
                        return response.bodyToMono(Response.class);
                    }
                    return Mono.empty();
                }).retryWhen(fixedDelay(MAX_ATTEMPTS, Duration.ofSeconds(2)));
        result.subscribe();
        return result;
    }

    @Override
    @SneakyThrows
    @Retry(name = "retryMailService")
    public Mono<Response> createMailbox(Credentials credentials) {
        log.info("Creating a Mailbox in Mail Server {} | User: {}, Mailbox: {}",
                mailServerName, credentials.getAddress(), inbox);

        var result = mailServerApiClient.put()
                .uri(USERS_PATH + credentials.getAddress() + "/mailboxes/" + inbox)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        log.info("Mailbox created in Mail Service | Status code: {} | Address: {}",
                                response.statusCode(), credentials.getAddress());
                        eventProducer.send(new Event<>(Type.MAILBOX_CREATED_IN_MAIL_SERVICE, credentials));
                        return response.bodyToMono(Response.class);
                    }
                    return Mono.empty();
                }).retryWhen(fixedDelay(MAX_ATTEMPTS, Duration.ofSeconds(2)));
        result.subscribe();
        return result;
    }

    @Override
    public Mono<Integer> getQuotaSize(String username) {
        log.info("Getting the quota size for a user | User: {}", username);

        var result = mailServerApiClient.get()
                .uri(quotaPath + username + "/size")
                .retrieve()
                .bodyToMono(Integer.class)
                .retryWhen(fixedDelay(MAX_ATTEMPTS, Duration.ofSeconds(DURATION_SECONDS)));
        result.subscribe();
        return result;
    }

    @Override
    public Mono<Response> updateQuotaSize(Credentials credentials) {
        log.info("Updating the quota size for a user | User: {}", credentials.getAddress());

        var result = mailServerApiClient.put()
                .uri(quotaPath + credentials.getAddress() + "/size")
                .bodyValue(Integer.parseInt(quotaSize))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        log.info("Quota size updated in Mail Service | Status code: {} | Address: {}",
                                response.statusCode(), credentials.getAddress());
                        eventProducer.send(new Event<>(Type.QUOTA_SIZE_UPDATED_IN_MAIL_SERVICE, credentials));
                        return response.bodyToMono(Response.class);
                    }
                    return Mono.empty();
                }).retryWhen(fixedDelay(MAX_ATTEMPTS, Duration.ofSeconds(DURATION_SECONDS)));
        result.subscribe();
        return result;
    }

    @Override
    public Mono<Integer> getUsedSize(String username) {
        log.info("Getting used size for a user | User: {}", username);

        var result = mailServerApiClient.get()
                .uri(quotaPath + username)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(fixedDelay(MAX_ATTEMPTS, Duration.ofSeconds(DURATION_SECONDS)))
                .map(jsonString -> {
                    try {
                        var quota = mapper.readTree(jsonString);
                        var occupation = quota.get("occupation");
                        return occupation.get("size").asInt();
                    } catch (JsonProcessingException ex) {
                        throw new MailboxNotFoundException();
                    }
                });
        result.subscribe();
        return result;
    }

}