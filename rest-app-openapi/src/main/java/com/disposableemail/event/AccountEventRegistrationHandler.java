package com.disposableemail.event;

import com.disposableemail.rest.model.Credentials;
import com.disposableemail.service.api.AccountService;
import com.disposableemail.service.api.auth.AuthorizationService;
import com.disposableemail.service.api.mail.MailServerClientService;
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
public class AccountEventRegistrationHandler {

    private final AuthorizationService authorizationService;
    private final MailServerClientService mailServerClientService;
    private final AccountService accountService;

    @Async
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.accounts}", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "${queues.account-start-creating}"),
            key = "${routing-keys.account-start-creating}"
    ))
    public void handleStartCreatingAccountEvent(Credentials credentials) {
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

    @Async
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.accounts}", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "${queues.account-in-mail-service-creating}"),
            key = "${routing-keys.account-in-mail-service-creating}"
    ))
    public void handleAccountInMailServiceCreatingEvent(Credentials credentials) {
        mailServerClientService.createMailbox(credentials);
    }

    @Async
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.accounts}", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "${queues.account-mailbox-in-mail-service-creating}"),
            key = "${routing-keys.account-mailbox-in-mail-service-creating}"
    ))
    public void handleAccountMailboxInMailServiceCreatingEvent(Credentials credentials) {
        mailServerClientService.updateQuotaSize(credentials);
    }

    @Async
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.accounts}", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "${queues.account-quota-in-mail-service-updating}"),
            key = "${routing-keys.account-quota-in-mail-service-updating}"
    ))
    public void handleAccountQuotaInMailServiceUpdatingEvent(Credentials credentials) {
        accountService.setMailboxId(credentials);
    }

}
