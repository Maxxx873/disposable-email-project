package com.disposableemail.event;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.rest.model.Credentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventProducer {
    private final RabbitTemplate rabbit;

    public void sendGettingMessages(AccountEntity accountEntity) {
        log.info("Event 'Getting messages' | Account: {}",  accountEntity.getAddress());
        rabbit.convertAndSend("messages", "getting-messages", accountEntity);
    }

    public void sendStartCreatingAccount(Credentials credentials) {
        log.info("Event 'Start creating account' | Account: {}",  credentials.getAddress());
        rabbit.convertAndSend("account-start-creating", "start-creating-account", credentials);
    }

    public void sendKeycloakConfirmation(Credentials credentials) {
        log.info("Event 'Keycloak confirmation account' | Account: {}",  credentials.getAddress());
        rabbit.convertAndSend("account-keycloak-confirmation", "keycloak-confirmation-account", credentials);
    }

    public void sendAccountCreatedInMailService(Credentials credentials) {
        log.info("Event 'Account in MailService created' | Account: {}",  credentials.getAddress());
        rabbit.convertAndSend("account-created-in-mail-service", "account-created-in-mail-service", credentials);
    }

    public void sendMailboxCreatedInMailService(Credentials credentials) {
        log.info("Event 'Mailbox in MailService created' | Account: {}",  credentials.getAddress());
        rabbit.convertAndSend("account-mailbox-created", "mailbox-created-in-mail-service", credentials);
    }

    public void sendQuotaSizeUpdatedInMailService(Credentials credentials) {
        log.info("Event 'Quota size updated in MailService' | Account: {}",  credentials.getAddress());
        rabbit.convertAndSend("account-quota-size-updated", "quota-size-updated-in-mail-service", credentials);
    }
}
