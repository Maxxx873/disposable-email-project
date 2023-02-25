package com.disposableemail.apache.james.mailet.collector;

import com.disposableemail.apache.james.mailet.collector.pojo.Account;
import com.disposableemail.apache.james.mailet.collector.pojo.MailMessage;
import com.disposableemail.apache.james.mailet.collector.pojo.Source;
import com.disposableemail.apache.james.mailet.collector.subscriber.MessageCollectorSubscriber;
import com.disposableemail.apache.james.mailet.collector.subscriber.SourceCollectorSubscriber;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

public class ReactiveSourceCollector extends BasicMailCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveSourceCollector.class);

    private MongoClient mongoClient;
    private MongoCollection<Source> sourceCollection;
    private MongoCollection<MailMessage> messageCollection;
    private MongoCollection<Account> accountCollection;


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
                sourceCollection.insertOne(getMailSource(mimeMessage)).subscribe(new SourceCollectorSubscriber<>());
                var message = getMailMessage(mimeMessage);

                message.getTo().forEach(address ->
                        accountCollection.find(eq("address", address.getAddress()))
                                .first()
                                .subscribe(new MessageCollectorSubscriber(messageCollection, message)));

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
        sourceCollection = database.getCollection(sourceCollectionName, Source.class);
        messageCollection = database.getCollection(messageCollectionName, MailMessage.class);
        accountCollection = database.getCollection(accountCollectionName, Account.class);
        var collectorSubscriber = new SourceCollectorSubscriber<>();
        mongoClient.listDatabases().subscribe(collectorSubscriber);
        database.listCollectionNames().subscribe(collectorSubscriber);
    }

}

