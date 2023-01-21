package com.disposableemail.apache.james.mailet.collector;

import com.disposableemail.apache.james.mailet.collector.pojo.MailSource;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

public class ReactiveSourceCollector extends BasicSourceCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveSourceCollector.class);

    private MongoClient mongoClient;
    private MongoCollection<MailSource> sourceCollection;

    @Override
    public void init() {
        LOGGER.info("{} Initializing...", this.getClass().getSimpleName());
        setupDatabase();
    }

    @Override
    public void destroy() {
        if (null != mongoClient) {
            LOGGER.info("Closing MongoDB connection...");
            mongoClient.close();
        }
        super.destroy();
    }

    @Override
    public Consumer<MimeMessage> processMimeMessage() {
        return mimeMessage -> {
            try {
                var out = new ByteArrayOutputStream();
                mimeMessage.writeTo(out);
                var mailSource = getMailSource(mimeMessage, out);
                sourceCollection.insertOne(mailSource).subscribe(new CollectorSubscriber<>());
                LOGGER.info("Added new data from message: {}", mimeMessage.getMessageID());
            } catch (IOException | MessagingException e) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public void setupDatabase() {
        super.initParameters();
        mongoClient = MongoClients.create(connectionString);
        var database = mongoClient.getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);
        sourceCollection = database.getCollection(collectionName, MailSource.class);
        var collectorSubscriber = new CollectorSubscriber<>();
        mongoClient.listDatabases().subscribe(collectorSubscriber);
        database.listCollectionNames().subscribe(collectorSubscriber);
    }

}

