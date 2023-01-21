package com.disposableemail.apache.james.mailet;

import com.disposableemail.apache.james.mailet.collector.SyncSourceCollector;
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
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class SyncSourceCollectorMailetTest {

    private SyncSourceCollector mailet;
    private static final String CONNECTION_STRING = "mongodb://%s:%d";
    private static final String DATABASE_NAME = "test";
    private static final String COLLECTION_NAME = "source";
    private MongodExecutable mongodExecutable;
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
        var mongoClient = MongoClients.create(String.format(CONNECTION_STRING, ip, port));

        mailet = new SyncSourceCollector();
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

        assertThat(mongoDatabase.getCollection(COLLECTION_NAME).countDocuments()).isZero();
    }

    @Test
    void mailetShouldCreateDocumentWhenMailWithoutAttachmentsIsNotEmpty() throws MessagingException {

        var mail = FakeMail.from(MimeMessageBuilder.mimeMessageBuilder().setText("test message").build());
        var collection = mongoDatabase.getCollection(COLLECTION_NAME);

        mailet.service(mail);

        var doc = collection.find().first();

        assertThat(doc).containsKey("msgid");
        assertThat(doc).containsKey("data");
        assertThat(doc).containsKey("attachments");
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

        assertThat(doc).containsKey("msgid");
        assertThat(doc).containsKey("data");
        assertThat(doc).containsKey("attachments");
        assertThat(Objects.requireNonNull(doc).getList("attachments", Object.class)).hasSize(getExpectedPartCount(multiPart));
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