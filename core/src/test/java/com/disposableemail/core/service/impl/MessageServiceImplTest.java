package com.disposableemail.core.service.impl;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.disposableemail.AbstractSpringIntegrationTest;
import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.dao.entity.MessageEntity;
import com.disposableemail.core.model.Address;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

class MessageServiceImplTest extends AbstractSpringIntegrationTest {

    private final int NUMBER_OF_DAYS = 7;

    private final AccountEntity account1 = AccountEntity.builder()
            .id("1")
            .address("recipient1@example.com")
            .quota(40000)
            .used(4000)
            .mailboxId("mailboxId1")
            .isDeleted(false)
            .isDisabled(true)
            .build();

    private final MessageEntity message1 = MessageEntity.builder()
            .id("1")
            .accountId("1")
            .msgid("msgid1")
            .from(List.of(Address.builder().name("sender1").address("sender1@example.com").build()))
            .to(List.of(Address.builder().name("recipient1").address("recipient1@example.com").build()))
            .cc(Collections.emptyList())
            .bcc(Collections.emptyList())
            .subject("subject")
            .isUnread(false)
            .isFlagged(false)
            .isDeleted(false)
            .text("text1")
            .hasAttachment(false)
            .attachments(Collections.emptyList())
            .size(600)
            .build();

    private final MessageEntity message2 = MessageEntity.builder()
            .id("2")
            .accountId("2")
            .msgid("msgid2")
            .from(List.of(Address.builder().name("sender2").address("sender2@example.com").build()))
            .to(List.of(Address.builder().name("recipient2").address("recipient2@example.com").build()))
            .cc(Collections.emptyList())
            .bcc(Collections.emptyList())
            .subject("subject")
            .isUnread(false)
            .isFlagged(false)
            .isDeleted(false)
            .text("text2")
            .hasAttachment(false)
            .attachments(Collections.emptyList())
            .size(700)
            .build();

    @BeforeEach
    public void setUp() {
        accountRepository.save(account1).block();
    }

    @AfterEach
    public void cleanUp() {
        messageRepository.deleteAll().block();
    }

    @Test
    void shouldSaveMessage() {
        messageService.saveMessage(message1).block();

        StepVerifier
                .create(messageRepository.findAll())
                .expectSubscription()
                .expectNextCount(1)
                .expectComplete()
                .verify();

    }

    @Test
    void shouldGetMessageById() {
        message1.setCreatedAt(Instant.now().minus(NUMBER_OF_DAYS, ChronoUnit.DAYS));
        message1.setUpdatedAt(Instant.now());
        messageService.saveMessage(message1).block();

        StepVerifier
                .create(messageService.getMessageById(message1.getId()))
                .expectSubscription()
                .assertNext(msg -> compareMessages(message1, msg))
                .expectComplete()
                .verify();

    }

    @Test
    @WithMockJwtAuth(authorities = {"recipient1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "recipient1@example.com"))
    void shouldGetMessage() {
        message1.setCreatedAt(Instant.now().minus(NUMBER_OF_DAYS, ChronoUnit.DAYS));
        message1.setUpdatedAt(Instant.now().minus(NUMBER_OF_DAYS, ChronoUnit.DAYS));

        messageService.saveMessage(message1).block();

        StepVerifier
                .create(messageService.getMessage(message1.getId()))
                .expectSubscription()
                .assertNext(msg -> compareMessages(message1, msg))
                .expectComplete()
                .verify();
    }

    @Test
    @WithMockJwtAuth(authorities = {"recipient2@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "recipient2@example.com"))
    void shouldThrowExceptionWhenGetMessageIfAuthorizedAnotherUser() {
        message1.setCreatedAt(Instant.now().minus(NUMBER_OF_DAYS, ChronoUnit.DAYS));
        messageService.saveMessage(message1).block();

        StepVerifier
                .create(messageService.getMessage(message1.getId()))
                .expectSubscription()
                .expectErrorMatches(throwable -> throwable instanceof AccessDeniedException)
                .verify();

    }

    @Test
    @WithMockJwtAuth(authorities = {"recipient1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "recipient1@example.com"))
    void shouldUpdateMessage() {
        message1.setCreatedAt(Instant.now().minus(NUMBER_OF_DAYS, ChronoUnit.DAYS));
        message1.setUpdatedAt(Instant.now().minus(NUMBER_OF_DAYS, ChronoUnit.DAYS));

        message2.setCreatedAt(Instant.now());
        message2.setUpdatedAt(Instant.now());

        messageService.saveMessage(message1).block();

        StepVerifier
                .create(messageService.updateMessage(message1.getId(), message2))
                .expectSubscription()
                .assertNext(msg -> compareMessages(message2, msg))
                .expectComplete()
                .verify();

    }

    @Test
    @WithMockJwtAuth(authorities = {"recipient2@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "recipient2@example.com"))
    void shouldThrowExceptionWhenUpdateMessageIfAuthorizedAnotherUser() {
        message1.setCreatedAt(Instant.now().minus(NUMBER_OF_DAYS, ChronoUnit.DAYS));
        message2.setCreatedAt(Instant.now());
        messageService.saveMessage(message1).block();

        StepVerifier
                .create(messageService.updateMessage(message1.getId(), message2))
                .expectSubscription()
                .expectErrorMatches(throwable -> throwable instanceof AccessDeniedException)
                .verify();

    }

    @Test
    @WithMockJwtAuth(authorities = {"recipient1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "recipient1@example.com"))
    void shouldSoftDeleteMessage() {
        message1.setCreatedAt(Instant.now().minus(NUMBER_OF_DAYS, ChronoUnit.DAYS));
        message1.setUpdatedAt(Instant.now());
        messageService.saveMessage(message1).block();
        message1.setIsDeleted(true);

        StepVerifier
                .create(messageService.softDeleteMessage(message1.getId()))
                .expectSubscription()
                .assertNext(msg -> compareMessages(message1, msg))
                .expectComplete()
                .verify();
    }

    @Test
    void shouldThrowExceptionWhenGetMessagesUnauthorizedUser() {
        int size = 1;
        int page = 0;

        message1.setCreatedAt(Instant.now().minus(NUMBER_OF_DAYS, ChronoUnit.DAYS));
        messageService.saveMessage(message1).block();

        StepVerifier
                .create(messageService.getMessagesForAuthorizedAccount(PageRequest.of(page, size)))
                .expectSubscription()
                .expectErrorMatches(throwable -> throwable instanceof AccessDeniedException)
                .verify();

    }

    @Test
    @WithMockJwtAuth(authorities = {"recipient1@example.com", "ROLE_USER"},
            claims = @OpenIdClaims(preferredUsername = "recipient1@example.com"))
    void shouldGetMessagesOnlyForAuthorizedAccount() {
        int size = 1;
        int page = 0;

        message1.setCreatedAt(Instant.now().minus(NUMBER_OF_DAYS, ChronoUnit.DAYS));
        message2.setCreatedAt(Instant.now());
        messageService.saveMessage(message1).block();
        messageService.saveMessage(message2).block();

        StepVerifier
                .create(messageService.getMessagesForAuthorizedAccount(PageRequest.of(page, size)))
                .expectSubscription()
                .expectNextCount(size)
                .expectComplete()
                .verify();

    }

    @Test
    void shouldDeleteOneMessageOlderNumberOfDays() {
        int size = 1;

        message1.setCreatedAt(Instant.now().minus(NUMBER_OF_DAYS, ChronoUnit.DAYS));
        message2.setCreatedAt(Instant.now());
        messageService.saveMessage(message1).block();
        messageService.saveMessage(message2).block();

        messageService.deleteMessagesOlderNumberOfDays(NUMBER_OF_DAYS).block();

        StepVerifier
                .create(messageRepository.findAll())
                .expectSubscription()
                .expectNextCount(size)
                .expectComplete()
                .verify();

    }

    @Test
    void shouldNotDeleteMessagesEarlierNumberOfDays() {
        int size = 2;

        message1.setCreatedAt(Instant.now());
        message2.setCreatedAt(Instant.now());
        messageService.saveMessage(message1).block();
        messageService.saveMessage(message2).block();

        messageService.deleteMessagesOlderNumberOfDays(NUMBER_OF_DAYS).block();

        StepVerifier
                .create(messageRepository.findAll())
                .expectSubscription()
                .expectNextCount(size)
                .expectComplete()
                .verify();

    }

    private void compareMessages(MessageEntity expected, MessageEntity actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAccountId(), actual.getAccountId());
        assertEquals(expected.getMsgid(), actual.getMsgid());
        assertEquals(expected.getFrom(), actual.getFrom());
        assertEquals(expected.getTo(), actual.getTo());
        assertEquals(expected.getCc(), actual.getCc());
        assertEquals(expected.getBcc(), actual.getBcc());
        assertEquals(expected.getSubject(), actual.getSubject());
        assertEquals(expected.getIsUnread(), actual.getIsUnread());
        assertEquals(expected.getIsFlagged(), actual.getIsFlagged());
        assertEquals(expected.getIsDeleted(), actual.getIsDeleted());
        assertEquals(expected.getText(), actual.getText());
        assertEquals(expected.getHasAttachment(), actual.getHasAttachment());
        assertEquals(expected.getAttachments(), actual.getAttachments());
        assertEquals(expected.getSize(), actual.getSize());
        assertEquals(getFormattedInstant(expected.getCreatedAt()), getFormattedInstant(actual.getCreatedAt()));
        assertThat(expected.getUpdatedAt()).isAfterOrEqualTo(getFormattedInstant(actual.getUpdatedAt()));
    }

    private String getFormattedInstant(Instant instant) {
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return instant.atZone(ZoneId.of("UTC")).format(formatter);
    }

}