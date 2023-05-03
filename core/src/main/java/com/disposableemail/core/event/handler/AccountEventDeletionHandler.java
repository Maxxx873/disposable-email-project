package com.disposableemail.core.event.handler;

import com.disposableemail.core.service.api.auth.AuthorizationService;
import com.disposableemail.core.service.api.mail.MailServerClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static org.springframework.amqp.core.ExchangeTypes.TOPIC;

@Component
@RequiredArgsConstructor
public class AccountEventDeletionHandler {

    private final AuthorizationService authorizationService;
    private final MailServerClientService mailServerClientService;

    @Async
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.accounts}", type = TOPIC),
            value = @Queue(name = "${queues.account-auth-deleting}"),
            key = "${routing-keys.account-auth-deleting}"
    ))
    public void handleAuthServiceDeletingAccountEvent(String id) {
        authorizationService.deleteUser(id);
    }

    @Async
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.accounts}", type = TOPIC),
            value = @Queue(name = "${queues.account-mail-deleting}"),
            key = "${routing-keys.account-mail-deleting}"
    ))
    public void handleMAilServerDeletingAccountEvent(String username) {
        mailServerClientService.deleteUser(username);
    }

}
