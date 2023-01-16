package com.disposableemail.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventProducer {

    @Value("${exchanges.messages}")
    private String messagesExchange;

    @Value("${exchanges.accounts}")
    private String accountsExchange;

    @Value("${routing-keys.messages-getting}")
    private String messagesGettingRoutingKey;

    @Value("${routing-keys.account-start-creating}")
    private String accountStartCrestingRoutingKey;

    @Value("${routing-keys.account-auth-confirmation}")
    private String accountAuthConfirmRoutingKey;

    @Value("${routing-keys.account-in-mail-service-creating}")
    private String accountInMailServiceCreatingRoutingKey;

    @Value("${routing-keys.account-mailbox-in-mail-service-creating}")
    private String accountMailboxInMailServiceCreatingRoutingKey;

    @Value("${routing-keys.account-quota-in-mail-service-updating}")
    private String accountQuotaInMailServiceUpdatingRoutingKey;

    private final RabbitTemplate rabbit;

    public void send(Event<?> event) {
        log.info("Event sent | {}", event.toString());

        switch (event.getType()) {
            case GETTING_MESSAGES ->
                    rabbit.convertAndSend(messagesExchange, messagesGettingRoutingKey, event.getData());
            case START_CREATING_ACCOUNT ->
                    rabbit.convertAndSend(accountsExchange, accountStartCrestingRoutingKey, event.getData());
            case KEYCLOAK_REGISTER_CONFIRMATION ->
                    rabbit.convertAndSend(accountsExchange, accountAuthConfirmRoutingKey, event.getData());
            case ACCOUNT_CREATED_IN_MAIL_SERVICE ->
                    rabbit.convertAndSend(accountsExchange, accountInMailServiceCreatingRoutingKey, event.getData());
            case MAILBOX_CREATED_IN_MAIL_SERVICE ->
                    rabbit.convertAndSend(accountsExchange, accountMailboxInMailServiceCreatingRoutingKey, event.getData());
            case QUOTA_SIZE_UPDATED_IN_MAIL_SERVICE ->
                    rabbit.convertAndSend(accountsExchange, accountQuotaInMailServiceUpdatingRoutingKey, event.getData());
            default -> throw new UnsupportedOperationException();
        }
    }
}