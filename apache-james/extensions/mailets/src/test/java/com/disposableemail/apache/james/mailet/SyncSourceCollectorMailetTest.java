package com.disposableemail.apache.james.mailet;

import com.disposableemail.apache.james.mailet.collector.SyncSourceCollector;
import com.disposableemail.apache.james.mailet.collector.pojo.Account;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.apache.mailet.base.test.FakeMail;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class SyncSourceCollectorMailetTest extends SourceCollectorTestHelper {

    private MongoDatabase mongoDatabase;

    @BeforeEach
    void setUp() throws Exception {
        mailet = new SyncSourceCollector();
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

        assertThat(mongoDatabase.getCollection(SOURCE_COLLECTION_NAME).countDocuments()).isZero();
    }

    @Test
    void mailetShouldCreateDocumentWhenMailWithoutAttachmentsIsNotEmpty() throws MessagingException, FileNotFoundException {
        var mimeMessage = new MimeMessage(null, new FileInputStream("src/test/resources/test_mail_html_no_attachments.eml"));
        var mail = FakeMail.defaultFakeMail();
        mail.setMessage(mimeMessage);
        var sourceCollection = mongoDatabase.getCollection(SOURCE_COLLECTION_NAME);
        var messageCollection = mongoDatabase.getCollection(MESSAGE_COLLECTION_NAME);
        var accountCollection = mongoDatabase.getCollection(ACCOUNT_COLLECTION_NAME, Account.class).withCodecRegistry(pojoCodecRegistry);

        account.setAddress("t3@example.com");
        accountCollection.insertOne(account);

        mailet.service(mail);

        var sourceDoc = sourceCollection.find().first();

        assertThat(sourceDoc).containsKeys("msgid", "data", "attachments");
        assertThat(Objects.requireNonNull(sourceDoc).getList("attachments", Object.class)).isEmpty();

        var messageDoc = messageCollection.find().first();

        assertThat(messageDoc).containsKeys("accountId", "msgid", "from", "to", "cc", "bcc", "subject",
                "attachments", "isUnread", "isFlagged", "isDeleted", "text", "html", "hasAttachment",
                "attachments", "size", "sentDate", "createdAt", "updatedAt");
        assertThat(Objects.requireNonNull(messageDoc).getList("attachments", Object.class)).isEmpty();
        assertThat(Objects.requireNonNull(messageDoc).get("text")).isEqualTo("Java test mail. No attachments");
        assertThat(ObjectId.isValid(Objects.requireNonNull(messageDoc).get("accountId").toString())).isEqualTo(true);
        assertThat(Objects.requireNonNull(messageDoc).get("isUnread")).isEqualTo(true);
        assertThat(Objects.requireNonNull(messageDoc).get("isUnread")).isEqualTo(true);
        assertThat(Objects.requireNonNull(messageDoc).get("isFlagged")).isEqualTo(false);
        assertThat(Objects.requireNonNull(messageDoc).get("isDeleted")).isEqualTo(false);
        assertThat(Objects.requireNonNull(messageDoc).get("hasAttachment")).isEqualTo(false);
        assertThat(Objects.requireNonNull(messageDoc).get("html").toString().contains(EXPECTED_HTML)).isTrue();
        assertThat(Integer.parseInt(Objects.requireNonNull(messageDoc).get("size").toString())).isGreaterThan(0);

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

        account.setAddress("test6@example.com");
        accountCollection.insertOne(account);

        mailet.service(mail);

        var sourceDoc = sourceCollection.find().first();

        assertThat(sourceDoc).containsKeys("msgid", "data", "attachments");
        assertThat(Objects.requireNonNull(sourceDoc).getList("attachments", Object.class)).hasSize(getExpectedPartCount(multiPart));

        var messageDoc = messageCollection.find().first();

        assertThat(messageDoc).containsKeys("accountId", "msgid", "from", "to", "cc", "bcc", "subject",
                "attachments", "isUnread", "isFlagged", "isDeleted", "text", "html", "hasAttachment",
                "attachments", "size", "sentDate", "createdAt", "updatedAt");
        assertThat(Objects.requireNonNull(messageDoc).get("text")).isEqualTo("test text message\n");
        assertThat(ObjectId.isValid(Objects.requireNonNull(messageDoc).get("accountId").toString())).isTrue();
        assertThat(Objects.requireNonNull(messageDoc).get("isUnread")).isEqualTo(true);
        assertThat(Objects.requireNonNull(messageDoc).get("isUnread")).isEqualTo(true);
        assertThat(Objects.requireNonNull(messageDoc).get("isFlagged")).isEqualTo(false);
        assertThat(Objects.requireNonNull(messageDoc).get("isDeleted")).isEqualTo(false);
        assertThat(Objects.requireNonNull(messageDoc).get("hasAttachment")).isEqualTo(true);
        assertThat(Integer.parseInt(Objects.requireNonNull(messageDoc).get("size").toString())).isPositive();
    }

}