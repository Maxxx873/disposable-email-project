package com.disposableemail.apache.james.mailet;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMailet;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class SourceCollectorMailet extends GenericMailet {

    private MongoClient mongoClient;
    private MongoCollection<Document> sourceCollection;

    @Override
    public void init() {
        System.out.println("---[Source Collector to MonngoDB Mailet]---");
        System.out.println("Initializing...");
        setupDatabase();
    }

    @Override
    public void service(Mail mail) throws MessagingException {
        MimeMessage message = mail.getMessage();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            message.writeTo(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var sourceDocument = new Document(Map.of("_id", new ObjectId(),"msgid", message.getMessageID(),
                "data", out.toString()));
        sourceCollection.insertOne(sourceDocument);
        System.out.printf("Added new data from message %s", mail.getMessage().getMessageID());
    }

    @Override
    public void destroy() {
        if (null != mongoClient) {
            System.out.println("Closing MongoDb connection...");
            mongoClient.close();
        }
        super.destroy();
    }

    /**
     * Set up the database with init parameters
     */
    void setupDatabase() {
        var connectionString = getInitParameter("connectionString");
        var databaseName = getInitParameter("database");
        var collectionName = getInitParameter("collectionName");
        var database = mongoClient.getDatabase(databaseName);

        mongoClient = MongoClients.create(connectionString);
        System.out.println("Database names:");
        mongoClient.listDatabases().forEach(System.out::println);
        System.out.println("Collection names:");
        database.listCollectionNames().forEach(System.out::println);
        sourceCollection = database.getCollection(collectionName);
    }

}
