package com.disposableemail.core.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@PropertySource("classpath:queues.properties")
public class EventProducer {

    @Value("${exchanges.messages}")
    private String messagesExchange;

    @Value("${exchanges.accounts}")
    private String accountsExchange;

    @Value("${exchanges.domains}")
    private String domainsExchange;

    @Value("${routing-keys.messages-getting}")
    private String messagesGettingRoutingKey;

    @Value("${routing-keys.account-start-creating}")
    private String accountStartCreatingRoutingKey;

    @Value("${routing-keys.account-auth-deleting}")
    private String accountAuthDeletingRoutingKey;

    @Value("${routing-keys.account-mail-deleting}")
    private String accountMailDeletingRoutingKey;

    @Value("${routing-keys.account-deleting}")
    private String accountDeletingRoutingKey;

    @Value("${routing-keys.account-auth-confirmation}")
    private String accountAuthConfirmRoutingKey;

    @Value("${routing-keys.account-in-mail-service-creating}")
    private String accountInMailServiceCreatingRoutingKey;

    @Value("${routing-keys.account-mailbox-in-mail-service-creating}")
    private String accountMailboxInMailServiceCreatingRoutingKey;

    @Value("${routing-keys.account-quota-in-mail-service-updating}")
    private String accountQuotaInMailServiceUpdatingRoutingKey;

    @Value("${routing-keys.domain-creating}")
    private String domainsCreatingRoutingKey;

    @Value("${routing-keys.domain-deleting}")
    private String domainsDeletingRoutingKey;


    private final RabbitTemplate rabbitTemplate;

    public void send(Event<?> event) {

        log.info("Event sent | {}", event.getLogString());

        switch (event.getType()) {
            case GETTING_MESSAGES ->
                    rabbitTemplate.convertAndSend(messagesExchange, messagesGettingRoutingKey, event.getData());
            case START_CREATING_ACCOUNT ->
                    rabbitTemplate.convertAndSend(accountsExchange, accountStartCreatingRoutingKey, event.getData());
            case AUTH_REGISTER_CONFIRMATION ->
                    rabbitTemplate.convertAndSend(accountsExchange, accountAuthConfirmRoutingKey, event.getData());
            case ACCOUNT_CREATED_IN_MAIL_SERVICE ->
                    rabbitTemplate.convertAndSend(accountsExchange, accountInMailServiceCreatingRoutingKey, event.getData());
            case MAILBOX_CREATED_IN_MAIL_SERVICE ->
                    rabbitTemplate.convertAndSend(accountsExchange, accountMailboxInMailServiceCreatingRoutingKey, event.getData());
            case QUOTA_SIZE_UPDATED_IN_MAIL_SERVICE ->
                    rabbitTemplate.convertAndSend(accountsExchange, accountQuotaInMailServiceUpdatingRoutingKey, event.getData());
            case AUTH_DELETING_ACCOUNT ->
                    rabbitTemplate.convertAndSend(accountsExchange, accountAuthDeletingRoutingKey, event.getData());
            case MAIL_DELETING_ACCOUNT ->
                    rabbitTemplate.convertAndSend(accountsExchange, accountMailDeletingRoutingKey, event.getData());
            case DELETING_ACCOUNT ->
                    rabbitTemplate.convertAndSend(accountsExchange, accountDeletingRoutingKey, event.getData());
            case DOMAIN_CREATED ->
                    rabbitTemplate.convertAndSend(domainsExchange, domainsCreatingRoutingKey, event.getData());
            case DOMAIN_DELETED ->
                    rabbitTemplate.convertAndSend(domainsExchange, domainsDeletingRoutingKey, event.getData());
            default -> throw new UnsupportedOperationException();
        }
    }
}
