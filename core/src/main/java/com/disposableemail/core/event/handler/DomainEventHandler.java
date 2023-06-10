package com.disposableemail.core.event.handler;

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
public class DomainEventHandler {

    private final MailServerClientService mailServerClientService;

    @Async
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.domains}", type = TOPIC),
            value = @Queue(name = "${queues.domain-creating}"),
            key = "${routing-keys.domain-creating}"
    ))
    public void handleDomainCreationEvent(String domain) {
        mailServerClientService.createDomain(domain);
    }

    @Async
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.domains}", type = TOPIC),
            value = @Queue(name = "${queues.domain-deleting}"),
            key = "${routing-keys.domain-deleting}"
    ))
    public void handleDomainDeletingEvent(String domain) {
        mailServerClientService.deleteDomain(domain);
    }
}
