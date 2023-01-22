package com.disposableemail.apache.james.mailet;

import com.disposableemail.apache.james.mailet.collector.BasicSourceCollector;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.mailet.base.test.FakeMailetConfig;
import org.junit.jupiter.api.BeforeEach;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

public class SourceCollectorTestHelper {

    protected static final String CONNECTION_STRING = "mongodb://%s:%d";
    protected static final String DATABASE_NAME = "test";
    protected static final String COLLECTION_NAME = "source";
    protected String ip = "localhost";
    protected int port = 2000;
    protected FakeMailetConfig mailetConfig;
    protected MongodExecutable mongodExecutable;
    protected MongodConfig mongodConfig;
    protected BasicSourceCollector mailet;

    @BeforeEach
    protected void setConfig() throws Exception {

        mongodConfig = MongodConfig
                .builder()
                .version(Version.Main.V3_6)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();

        mailetConfig = FakeMailetConfig.builder()
                .mailetName("SourceCollector")
                .setProperty("connectionString", String.format(CONNECTION_STRING, ip, port))
                .setProperty("database", DATABASE_NAME)
                .setProperty("collectionName", COLLECTION_NAME)
                .build();

        var starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();

    }

    protected static int getExpectedPartCount(Multipart multiPart) throws MessagingException {
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
