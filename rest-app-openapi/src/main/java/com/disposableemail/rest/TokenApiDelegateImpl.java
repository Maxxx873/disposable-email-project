package com.disposableemail.rest;

import com.disposableemail.rest.api.TokenApiDelegate;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;
import com.disposableemail.service.api.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenApiDelegateImpl implements TokenApiDelegate {

    private final AuthorizationService authorizationService;
    @Override
    public ResponseEntity<Token> postCredentialsItem(Credentials credentials) {
        log.info("Keycloak | {}", authorizationService.getToken(credentials));
        return new ResponseEntity<>(authorizationService.getToken(credentials), HttpStatus.OK);
    }
}
