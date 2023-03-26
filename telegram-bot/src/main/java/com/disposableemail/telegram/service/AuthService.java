package com.disposableemail.telegram.service;

import com.disposableemail.telegram.client.disposableemail.webclient.api.DefaultApi;
import com.disposableemail.telegram.client.disposableemail.webclient.model.Credentials;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final DefaultApi api;
    private final EmailService emailService;

    private final TextEncryptor encryptor;

    public Mono<String> authenticate(AccountEntity account) {
        var credentials = new Credentials();
        credentials.setAddress(account.getAddress());
        credentials.setPassword(encryptor.decrypt(account.getPassword()));
        return emailService.getToken(credentials)
                .map(token -> {
                    api.getApiClient().setBearerToken(token);
                    return token;
                });
    }

    public Mono<String> authenticate(Credentials credentials) {
        return emailService.getToken(credentials)
                .map(token -> {
                    api.getApiClient().setBearerToken(token);
                    return token;
                });
    }
}