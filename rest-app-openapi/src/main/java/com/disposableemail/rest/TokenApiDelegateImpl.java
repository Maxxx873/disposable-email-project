package com.disposableemail.rest;

import com.disposableemail.rest.api.TokenApiDelegate;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;
import com.disposableemail.service.api.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenApiDelegateImpl implements TokenApiDelegate {

    private final AccountService accountService;

    @Override
    public Mono<ResponseEntity<Token>> postCredentialsItem(Mono<Credentials> credentials, ServerWebExchange exchange) {
        return credentials.flatMap(accountService::getTokenFromAuthorizationService)
                .map(token -> ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(token));
    }

}
