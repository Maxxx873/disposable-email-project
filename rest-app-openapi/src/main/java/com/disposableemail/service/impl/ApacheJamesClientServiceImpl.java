package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.DomainEntity;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.service.api.MailServerClientService;
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
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApacheJamesClientServiceImpl implements MailServerClientService {

    @Value("${mail-server.name}")
    private String mailServerName;

    private final ObjectMapper mapper;

    private final WebClient mailServerApiClient;

    private final RetryRegistry registry;

    @PostConstruct
    public void postConstruct() {
        registry.retry("retryMailService").getEventPublisher().onRetry(ev -> log.info("Connect to {} API: {}", mailServerName, ev));
    }

    @Override
    @Retry(name = "retryMailService", fallbackMethod = "getMockDomains")
    public Flux<DomainEntity> getDomains() {

        return mailServerApiClient.get().uri("/domains").retrieve().bodyToMono(String.class).map(response -> {
            try {
                log.info("Getting domains from {} | {}", mailServerName, response);
                return mapper.readValue(response, new TypeReference<List<String>>() {
                });
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }).flatMapIterable(list -> list).map(domainName -> DomainEntity.builder()
                .domain(domainName)
                .isActive(true)
                .isPrivate(false)
                .build());
    }

    @Override
    @SneakyThrows
    public Response createUser(Credentials credentials) {
        log.info("Creating a User in Mail Server {} | User: {}", mailServerName, credentials.getAddress());

        var user = mapper.createObjectNode();
        user.put("password", credentials.getPassword());

        return mailServerApiClient.put()
                .uri("/users/" + credentials.getAddress())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(mapper.writeValueAsString(user)), String.class)
                .retrieve()
                .bodyToMono(Response.class)
                .block();
    }

    public Flux<DomainEntity> getMockDomains(Throwable t) {
        log.info("Getting mock domains | Not available mail server {}", mailServerName);

        var domainList = Arrays.asList(
                DomainEntity.builder()
                        .isPrivate(true)
                        .isActive(true)
                        .domain("example.com")
                        .build(),
                DomainEntity.builder()
                        .isPrivate(true)
                        .isActive(true)
                        .domain("example.org")
                        .build()

        );
        return Flux.just(domainList).flatMapIterable(list -> list);
    }
}
