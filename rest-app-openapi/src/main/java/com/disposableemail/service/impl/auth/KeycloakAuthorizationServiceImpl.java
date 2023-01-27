package com.disposableemail.service.impl.auth;

import com.disposableemail.event.Event;
import com.disposableemail.event.EventProducer;
import com.disposableemail.exception.AccountAlreadyRegisteredException;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;
import com.disposableemail.service.api.auth.AuthorizationService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static com.disposableemail.event.Event.Type.KEYCLOAK_REGISTER_CONFIRMATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthorizationServiceImpl implements AuthorizationService {

    @Value("${keycloak.server.realm}")
    private String realm;
    @Value("${keycloak.server.url}")
    private String serverUrl;
    @Value("${keycloak.server.client}")
    private String client;
    private final Keycloak keycloak;
    private final UserRepresentation userRepresentation;
    private final CredentialRepresentation credentialRepresentation;
    private final EventProducer eventProducer;
    private final TextEncryptor encryptor;

    @Async
    @Override
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.accounts}", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "${queues.account-start-creating}"),
            key = "${routing-keys.account-start-creating}"
    ))
    public CompletableFuture<Response> createUser(Credentials credentials, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        userRepresentation.setUsername(credentials.getAddress());
        credentialRepresentation.setValue(encryptor.decrypt(credentials.getPassword()));
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
        channel.basicAck(tag, false);
        return CompletableFuture.completedFuture(getKeycloakResponse(credentials));
    }

    @Override
    public Token getToken(Credentials credentials) {
        log.info("Keycloak | Getting Token string from a Token Manager for {}", credentials.getAddress());
        var instance = Keycloak.getInstance(serverUrl, realm, credentials.getAddress(),
                credentials.getPassword(), client);
        var tokenManager = instance.tokenManager();
        return new Token(tokenManager.getAccessTokenString());
    }

    private Response getKeycloakResponse(Credentials credentials) {
        var response = keycloak.realm(realm).users().create(userRepresentation);
        if (response.getStatusInfo().equals(Response.Status.CONFLICT)) {
            log.error("Keycloak |  User: {} | Status: {} | Status Info: {}", userRepresentation.getUsername(),
                    response.getStatus(), response.getStatusInfo());
            throw new AccountAlreadyRegisteredException();
        }
        if (response.getStatusInfo().equals(Response.Status.CREATED)) {
            eventProducer.send(new Event<>(KEYCLOAK_REGISTER_CONFIRMATION, credentials));
        }
        log.info("Keycloak |  User: {} | Status: {} | Status Info: {}", userRepresentation.getUsername(),
                response.getStatus(), response.getStatusInfo());
        return response;
    }
}