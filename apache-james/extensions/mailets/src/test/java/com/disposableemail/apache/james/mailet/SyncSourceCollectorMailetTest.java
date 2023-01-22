package com.disposableemail.apache.james.mailet;

import com.disposableemail.apache.james.mailet.collector.SyncSourceCollector;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.apache.james.core.builder.MimeMessageBuilder;
import org.apache.mailet.base.test.FakeMail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
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

        assertThat(mongoDatabase.getCollection(COLLECTION_NAME).countDocuments()).isZero();
    }

    @Test
    void mailetShouldCreateDocumentWhenMailWithoutAttachmentsIsNotEmpty() throws MessagingException {

        var mail = FakeMail.from(MimeMessageBuilder.mimeMessageBuilder().setText("test message").build());
        var collection = mongoDatabase.getCollection(COLLECTION_NAME);

        mailet.service(mail);

        var doc = collection.find().first();

        assertThat(doc).containsKeys("msgid", "data", "attachments");
        assertThat(Objects.requireNonNull(doc).getList("attachments", Object.class)).isEmpty();
    }

    @Test
    void mailetShouldCreateDocumentWhenMailHasAttachments() throws MessagingException, IOException {
        var mail = FakeMail.defaultFakeMail();
        var mimeMessage = new MimeMessage(null, new FileInputStream("src/test/resources/test-mail.eml"));
        var multiPart = (Multipart) mimeMessage.getContent();
        mail.setMessage(mimeMessage);
        var collection = mongoDatabase.getCollection(COLLECTION_NAME);

        mailet.service(mail);

        var doc = collection.find().first();

        assertThat(doc).containsKeys("msgid", "data", "attachments");
        assertThat(Objects.requireNonNull(doc).getList("attachments", Object.class)).hasSize(getExpectedPartCount(multiPart));
    }

}