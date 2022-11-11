package com.disposableemail.service.impl.mail;

import com.disposableemail.dao.entity.DomainEntity;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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

    private final String MAILBOX_ID_KEY = "mailboxId";
    private final String PASSWORD_FIELD = "password";
    private final ObjectMapper mapper;
    private final WebClient mailServerApiClient;
    private final RetryRegistry registry;


    @PostConstruct
    public void postConstruct() {
        registry.retry("retryMailService").getEventPublisher().onRetry(ev ->
                log.info("Connect to {} API: {}", mailServerName, ev));
    }

    @Override
    @Retry(name = "retryMailService")
    public Mono<String> getMailboxId(Credentials credentials, String mailboxName) {

        return mailServerApiClient.get().uri("/users/{username}/mailboxes", credentials.getAddress())
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

    @Override
    @SneakyThrows
    @Retry(name = "retryMailService")
    public Mono<Response> createUser(Credentials credentials) {
        log.info("Creating a User in Mail Server {} | User: {}", mailServerName, credentials.getAddress());

        var password = mapper.createObjectNode();
        password.put(PASSWORD_FIELD, credentials.getPassword());
        var response = mailServerApiClient.put()
                .uri("/users/" + credentials.getAddress())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(password))
                .retrieve()
                .bodyToMono(Response.class);

        response.subscribe();

        return response;
    }

    @Override
    @SneakyThrows
    @Retry(name = "retryMailService")
    public Mono<Response> createMailbox(Credentials credentials, String mailboxName) {
        log.info("Creating a Mailbox in Mail Server {} | User: {}, Mailbox: {}",
                mailServerName, credentials.getAddress(), mailboxName);

        var response = mailServerApiClient.put()
                .uri("/users/" + credentials.getAddress() + "/mailboxes/" + mailboxName)
                .retrieve()
                .bodyToMono(Response.class)
                .retryWhen(reactor.util.retry.Retry.fixedDelay(3, Duration.ofSeconds(2)));
        response.subscribe();

        return response;
    }

    @Override
    public Mono<Integer> getQuotaSize(String username) {
        log.info("Getting the quota size for a user | User: {}", username);

        var response = mailServerApiClient.get()
                .uri("/quota/users/" + username + "/size")
                .retrieve()
                .bodyToMono(Integer.class)
                .retryWhen(reactor.util.retry.Retry.fixedDelay(3, Duration.ofSeconds(2)));
        response.subscribe();

        return response;
    }

    @Override
    public Mono<Response> updateQuotaSize(Credentials credentials, int quotaSize) {
        log.info("Updating the quota size for a user | User: {}", credentials.getAddress());

        var response = mailServerApiClient.put()
                .uri("/quota/users/" + credentials.getAddress() + "/size")
                .bodyValue(quotaSize)
                .retrieve()
                .bodyToMono(Response.class)
                .retryWhen(reactor.util.retry.Retry.fixedDelay(3, Duration.ofSeconds(2)));
        response.subscribe();

        return response;
    }

    @Override
    public Mono<Integer> getUsedSize(String username) {
        log.info("Getting used size for a user | User: {}", username);

        var response = mailServerApiClient.get()
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
        response.subscribe();

        return response;
    }

}
