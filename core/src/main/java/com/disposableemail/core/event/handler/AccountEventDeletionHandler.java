package com.disposableemail.core.event.handler;

import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.api.auth.AuthorizationService;
import com.disposableemail.core.service.api.mail.MailServerClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountEventDeletionHandler {

    private final AuthorizationService authorizationService;
    private final MailServerClientService mailServerClientService;

    @Async
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.accounts}", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "${queues.account-start-creating}"),
            key = "${routing-keys.account-start-creating}"
    ))
    public void handleStartDeletingAccountEvent(Credentials credentials) {
        authorizationService.createUser(credentials);
    }

    @Async
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.accounts}", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "${queues.account-auth-confirmation}"),
            key = "${routing-keys.account-auth-confirmation}"
    ))
    public void handleKeycloakRegisterConfirmationEvent(Credentials credentials) throws JsonProcessingException {
        mailServerClientService.createUser(credentials);
    }

}
