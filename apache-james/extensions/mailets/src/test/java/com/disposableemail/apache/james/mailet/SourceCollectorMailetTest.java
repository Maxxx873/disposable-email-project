package com.disposableemail.apache.james.mailet;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.james.core.builder.MimeMessageBuilder;
import org.apache.mailet.base.test.FakeMail;
import org.apache.mailet.base.test.FakeMailetConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class SourceCollectorMailetTest {

    private SourceCollector mailet;
    private static final String CONNECTION_STRING = "mongodb://%s:%d";
    private static final String DATABASE_NAME = "test";
    private static final String COLLECTION_NAME = "source";

    private MongodExecutable mongodExecutable;
    private MongoClient mongoClient;

    private MongoDatabase mongoDatabase;

    @BeforeEach
    void setUp() throws Exception {
        String ip = "localhost";
        int port = 2000;

        var mongodConfig = MongodConfig
                .builder()
                .version(Version.Main.V3_6)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();
        var starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        mongoClient = MongoClients.create(String.format(CONNECTION_STRING, ip, port));

        mailet = new SourceCollector();
        mailet.init(FakeMailetConfig.builder()
                .mailetName("SourceCollector")
                .setProperty("connectionString", String.format(CONNECTION_STRING, ip, port))
                .setProperty("database", DATABASE_NAME)
                .setProperty("collectionName", COLLECTION_NAME)
                .build());

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

        assertThat(mongoDatabase.getCollection(COLLECTION_NAME).countDocuments()).isEqualTo(0);
    }

    @Test
    void mailetShouldCreateDocumentWhenMailWithoutAttachmentsIsNotEmpty() throws MessagingException {

        var mail = FakeMail.from(MimeMessageBuilder.mimeMessageBuilder().setText("test message").build());
        var collection = mongoDatabase.getCollection(COLLECTION_NAME);

        mailet.service(mail);

        var doc = collection.find().first();

        assertThat(doc.containsKey("msgid")).isTrue();
        assertThat(doc.containsKey("data")).isTrue();
        assertThat(doc.containsKey("attachments")).isTrue();
        assertThat(doc.getList("attachments", Object.class).size()).isEqualTo(0);
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

        assertThat(doc.containsKey("msgid")).isTrue();
        assertThat(doc.containsKey("data")).isTrue();
        assertThat(doc.containsKey("attachments")).isTrue();
        assertThat(doc.getList("attachments", Object.class).size()).isEqualTo(getExpectedPartCount(multiPart));
    }

    private static int getExpectedPartCount(Multipart multiPart) throws MessagingException {
        int expectedPartCount = 0;
        for (int partCount = 0; partCount < multiPart.getCount(); partCount++) {
            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                ++expectedPartCount;
            }
        }
        return expectedPartCount;
    }


}