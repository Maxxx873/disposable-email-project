package com.disposableemail.service.api.auth;


import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface AuthorizationService {

    CompletableFuture<Response> createUser(Credentials credentials, Channel channel,
                                           @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException;

    Token getToken(Credentials credentials);
}
