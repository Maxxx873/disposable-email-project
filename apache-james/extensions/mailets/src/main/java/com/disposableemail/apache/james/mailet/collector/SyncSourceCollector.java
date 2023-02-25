package com.disposableemail.apache.james.mailet.collector;


import com.disposableemail.apache.james.mailet.collector.pojo.Account;
import com.disposableemail.apache.james.mailet.collector.pojo.MailMessage;
import com.disposableemail.apache.james.mailet.collector.pojo.Source;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

public class SyncSourceCollector extends BasicMailCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncSourceCollector.class);

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
                sourceCollection.insertOne(getMailSource(mimeMessage));
                var message = getMailMessage(mimeMessage);

                message.getTo().forEach(address -> {
                    var accountId = Optional.ofNullable(accountCollection
                            .find(eq("address", address.getAddress())).first())
                            .orElseThrow(IllegalArgumentException::new)
                            .getId();
                    message.setAccountId(accountId.toString());
                    messageCollection.insertOne(message);
                });

                LOGGER.info("Added new data from message {}", mimeMessage.getMessageID());
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
        LOGGER.info("Database names:");
        mongoClient.listDatabases().forEach(databaseName -> LOGGER.info(databaseName.toString()));
        LOGGER.info("Collection names:");
        database.listCollectionNames().forEach(LOGGER::info);
    }

}