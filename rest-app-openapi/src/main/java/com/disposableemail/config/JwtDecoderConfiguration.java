package com.disposableemail.config;

import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.HttpURLConnection;
import java.util.Objects;

@Slf4j
@Configuration
public class JwtDecoderConfiguration {

    @Autowired
    private ApplicationContext context;
    @Autowired
    private RetryRegistry registry;

    @PostConstruct
    public void postConstruct() {
        registry.retry("retryAuthorizationService").getEventPublisher().onRetry(ev -> log.info("messsage: {}", ev));
    }

    @Retry(name = "retryAuthorizationService", fallbackMethod = "exitApplication")
    public ReactiveJwtDecoder getReactiveJwtDecoder(String issuerUri) {
        log.info("Getting issuerUri | connect to AuthorizationService {}", issuerUri);
        var httpStatus = WebClient.builder().baseUrl(issuerUri).build()
                .get().exchangeToMono(response -> Mono.just(response.statusCode()))
                .block();
        var statusCode = Objects.requireNonNull(httpStatus).value();
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw new org.springframework.web.client.HttpServerErrorException(httpStatus);
        }
        return ReactiveJwtDecoders.fromOidcIssuerLocation(issuerUri);
    }

    public ReactiveJwtDecoder exitApplication(String issuerUri, Throwable t) {
        log.info("Application exits | Not available issuerUri {}", issuerUri);
        System.exit(SpringApplication.exit(context, () -> 0));
        return null;
    }
}
