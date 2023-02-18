package com.disposableemail.core.event.handler;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.service.api.util.ElasticMongoIntegrationService;
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
public class MessagesEventHandler {

    private final ElasticMongoIntegrationService integrationService;

    @Async
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.messages}", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "${queues.messages-getting}"),
            key = "${routing-keys.messages-getting}"
    ))
    public void handleGettingMessagesEvent(AccountEntity accountEntity) {
        integrationService.saveMessagesFromElasticsearchMailboxToMongo(accountEntity);
    }
}
