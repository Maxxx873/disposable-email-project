package com.disposableemail.apache.james.mailet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import org.apache.mailet.base.test.FakeMail;
import org.awaitility.Durations;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.disposableemail.apache.james.mailet.collector.ReactiveSourceCollector;
import com.disposableemail.apache.james.mailet.collector.pojo.Account;
import com.disposableemail.apache.james.mailet.collector.subscriber.SourceCollectorSubscriber;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;

import reactor.test.StepVerifier;

class ReactiveSourceCollectorMailetTest extends SourceCollectorTestHelper {

    private MongoDatabase mongoDatabase;

    @BeforeEach
    void setUp() throws Exception {
        mailet = new ReactiveSourceCollector();
        mailet.init(mailetConfig);

        var mongoClient = MongoClients.create(String.format(CONNECTION_STRING, ip, port));
        mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);

    }

    @AfterEach
    void clean() {
        mongodExecutable.stop();
    }

    @Test
    void mailetShouldNotCreateDocumentWhenMailIsEmpty() throws MessagingException {

        var mail = FakeMail.defaultFakeMail();

        mailet.service(mail);

        StepVerifier.create(mongoDatabase.getCollection(SOURCE_COLLECTION_NAME).countDocuments())
                .expectSubscription()
                .expectNext(0L)
                .expectComplete()
                .verify();
    }

    @Test
    void mailetShouldCreateDocumentWhenMailWithoutAttachmentsIsNotEmpty() throws MessagingException, FileNotFoundException {
        var mimeMessage = new MimeMessage(null, new FileInputStream("src/test/resources/test_mail_html_no_attachments.eml"));
        var mail = FakeMail.defaultFakeMail();
        mail.setMessage(mimeMessage);
        mail.setMessageSize(600);
        var sourceCollection = mongoDatabase.getCollection(SOURCE_COLLECTION_NAME);
        var messageCollection = mongoDatabase.getCollection(MESSAGE_COLLECTION_NAME);
        var accountCollection = mongoDatabase.getCollection(ACCOUNT_COLLECTION_NAME, Account.class).withCodecRegistry(pojoCodecRegistry);

        long initUsed = 500;
        long quota = 40000;
        var updatedAt = Instant.now();
        account.setAddress("t3@example.com");
        account.setUsed(initUsed);
        account.setQuota(quota);
        account.setUpdatedAt(updatedAt);
        accountCollection.insertOne(account).subscribe(new SourceCollectorSubscriber<>());

        await().pollDelay(Durations.ONE_SECOND).until(() -> true);

        mailet.service(mail);

        await().pollDelay(Durations.ONE_SECOND).until(() -> true);

        StepVerifier.create(sourceCollection.find().first())
                .expectSubscription()
                .assertNext(doc -> {
                    assertThat(doc).containsKeys("msgid", "data", "attachments");
                    assertThat(Objects.requireNonNull(doc).getList("attachments", Object.class)).isEmpty();
                })
                .expectComplete()
                .verify();

        StepVerifier.create(messageCollection.find().first())
                .expectSubscription()
                .assertNext(doc -> {
                    assertThat(doc).containsKeys("accountId", "msgid", "from", "to", "cc", "bcc", "subject",
                            "attachments", "isUnread", "isFlagged", "isDeleted", "text", "html", "hasAttachment",
                            "attachments", "size", "sentDate", "createdAt", "updatedAt");
                    assertThat(Objects.requireNonNull(doc).getList("attachments", Object.class)).isEmpty();
                    assertThat(Objects.requireNonNull(doc).get("text").toString()).contains("Java test mail. No attachments");
                    assertThat(simpleDateFormat.format(Objects.requireNonNull(doc).get("sentDate")))
                            .isEqualTo("2023-02-15T20:16:53Z");
                    assertThat(ObjectId.isValid(Objects.requireNonNull(doc).get("accountId").toString())).isTrue();
                    assertThat(getBoolean(doc, "isFlagged")).isFalse();
                    assertThat(getBoolean(doc, "isDeleted")).isFalse();
                    assertThat(getBoolean(doc, "hasAttachment")).isFalse();
                    assertThat(Objects.requireNonNull(doc).get("html").toString()).contains(EXPECTED_HTML);
                    assertThat(Integer.parseInt(Objects.requireNonNull(doc).get("size").toString())).isPositive();
                })
                .expectComplete()
                .verify();

        StepVerifier.create(accountCollection.find().first())
                .expectSubscription()
                .assertNext(acc -> {
                    assertThat(acc.getUsed()).isEqualTo(mail.getMessageSize() + account.getUsed());
                    assertThat(acc.getUpdatedAt()).isAfter(updatedAt);
                    assertThat(acc.getQuota()).isEqualTo(quota);
                })
                .expectComplete()
                .verify();

    }

    @Test
    void mailetShouldCreateDocumentWhenMailHasAttachments() throws MessagingException, IOException {
        var mail = FakeMail.defaultFakeMail();
        var mimeMessage = new MimeMessage(null, new FileInputStream("src/test/resources/test_mail_with_attachments.eml"));
        var multiPart = (Multipart) mimeMessage.getContent();
        mail.setMessage(mimeMessage);
        var sourceCollection = mongoDatabase.getCollection(SOURCE_COLLECTION_NAME);
        var messageCollection = mongoDatabase.getCollection(MESSAGE_COLLECTION_NAME);
        var accountCollection = mongoDatabase.getCollection(ACCOUNT_COLLECTION_NAME, Account.class).withCodecRegistry(pojoCodecRegistry);

        long initUsed = 500;
        long quota = 40000;
        account.setAddress("test6@example.com");
        account.setUsed(initUsed);
        account.setQuota(quota);
        var updatedAt = Instant.now();
        account.setUpdatedAt(Instant.now());
        accountCollection.insertOne(account).subscribe(new SourceCollectorSubscriber<>());

        await().pollDelay(Durations.ONE_SECOND).until(() -> true);

        mailet.service(mail);

        await().pollDelay(Durations.ONE_SECOND).until(() -> true);

        StepVerifier.create(sourceCollection.find().first())
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(10))
                .assertNext(doc -> {
                    assertThat(doc).containsKeys("msgid", "data", "attachments");
                    try {
                        assertThat(Objects.requireNonNull(doc).getList("attachments", Object.class)).hasSize(getExpectedPartCount(multiPart));
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .expectComplete()
                .verify();

        StepVerifier.create(messageCollection.find().first())
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(10))
                .assertNext(doc -> {
                    assertThat(doc).containsKeys("accountId", "msgid", "from", "to", "cc", "bcc", "subject",
                            "attachments", "isUnread", "isFlagged", "isDeleted", "text", "html", "hasAttachment",
                            "attachments", "size", "sentDate", "createdAt", "updatedAt");
                    try {
                        assertThat(Objects.requireNonNull(doc).getList("attachments", Object.class))
                                .hasSize(getExpectedPartCount(multiPart));
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                    assertThat(Objects.requireNonNull(doc).get("text").toString()).contains("test text message\n");
                    assertThat(simpleDateFormat.format(Objects.requireNonNull(doc).get("sentDate")))
                            .isEqualTo("2022-11-13T19:41:43Z");
                    assertThat(ObjectId.isValid(Objects.requireNonNull(doc).get("accountId").toString())).isTrue();
                    assertThat(getBoolean(doc, "isUnread")).isTrue();
                    assertThat(getBoolean(doc, "isFlagged")).isFalse();
                    assertThat(getBoolean(doc, "isDeleted")).isFalse();
                    assertThat(getBoolean(doc, "hasAttachment")).isTrue();
                    assertThat(Integer.parseInt(Objects.requireNonNull(doc).get("size").toString())).isPositive();
                })
                .expectComplete()
                .verify();

        StepVerifier.create(accountCollection.find().first())
                .expectSubscription()
                .assertNext(acc -> {
                    assertThat(acc.getUsed()).isEqualTo(mail.getMessageSize() + account.getUsed());
                    assertThat(acc.getUpdatedAt()).isAfter(updatedAt);
                    assertThat(acc.getQuota()).isEqualTo(quota);
                })
                .expectComplete()
                .verify();
    }

}