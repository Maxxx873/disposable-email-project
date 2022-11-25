package com.disposableemail.apache.james.mailet;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMailet;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SourceCollector extends GenericMailet {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceCollector.class);

    private MongoClient mongoClient;
    private MongoCollection<Document> sourceCollection;

    private final long PAYLOAD_DOCUMENT_MAX_SIZE = 16777216;

    @Override
    public void init() {
        LOGGER.info("---[Source Collector to MonngoDB Mailet]---");
        LOGGER.info("Initializing...");
        setupDatabase();
    }

    @Override
    public void service(Mail mail) throws MessagingException {
        if (mail.getMessageSize() < PAYLOAD_DOCUMENT_MAX_SIZE) {
            var optionalMimeMessage = Optional.ofNullable(mail.getMessage());
            optionalMimeMessage.ifPresent(getMimeMessageConsumer());
        }
    }

    private Consumer<MimeMessage> getMimeMessageConsumer() {
        return mimeMessage -> {
            var out = new ByteArrayOutputStream();
            try {
                mimeMessage.writeTo(out);
                var sourceDocument = new Document(Map.of(
                        "_id", new ObjectId(),
                        "msgid", mimeMessage.getMessageID(),
                        "data", out.toString(),
                        "attachments", getAttachments(mimeMessage),
                        "createdAt", LocalDateTime.now()
                ));
                sourceCollection.insertOne(sourceDocument);
                System.out.printf("Added new data from message %s", mimeMessage.getMessageID());
            } catch (IOException | MessagingException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public void destroy() {
        if (null != mongoClient) {
            System.out.println("Closing MongoDB connection...");
            mongoClient.close();
        }
        super.destroy();
    }

    /**
     * Set up the database with init parameters
     */
    private void setupDatabase() {
        var connectionString = getInitParameter("connectionString");
        var databaseName = getInitParameter("database");
        var collectionName = getInitParameter("collectionName");
        mongoClient = MongoClients.create(connectionString);
        var database = mongoClient.getDatabase(databaseName);
        System.out.println("Database names:");
        mongoClient.listDatabases().forEach(System.out::println);
        System.out.println("Collection names:");
        database.listCollectionNames().forEach(System.out::println);
        sourceCollection = database.getCollection(collectionName);
    }

    /**
     * Getting information about attachments as a list of documents from a mime message
     */
    private List<Document> getAttachments(Message message) throws IOException, MessagingException {
        var attachments = new ArrayList<Document>();
        var content = message.getContent();
        if (content instanceof Multipart) {
            var multiPart = (Multipart) content;
            for (int partCount = 0; partCount < multiPart.getCount(); partCount++) {
                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    var attachmentDocument = new Document(Map.of(
                            "_id", new ObjectId(),
                            "filename", part.getFileName(),
                            "contentType", getShortContentType(part.getContentType()),
                            "disposition", part.getDisposition(),
                            "transferEncoding", part.getEncoding(),
                            "size", part.getSize(),
                            "partId", partCount
                    ));
                    attachments.add(attachmentDocument);
                }
            }
        }
        return attachments;
    }

    private static String getShortContentType(String contentType) {
        var items = contentType.split(";");
        return items[0];
    }

    @Override
    public String getMailetInfo() {
        return SourceCollector.class.getName() + " Mailet";
    }


}
