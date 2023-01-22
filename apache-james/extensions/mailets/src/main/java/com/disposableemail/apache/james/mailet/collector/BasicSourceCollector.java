package com.disposableemail.apache.james.mailet.collector;


import com.disposableemail.apache.james.mailet.collector.pojo.MailAttachment;
import com.disposableemail.apache.james.mailet.collector.pojo.MailSource;
import com.mongodb.MongoClientSettings;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMailet;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

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
import java.util.Optional;
import java.util.function.Consumer;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class BasicSourceCollector extends GenericMailet {

    protected String connectionString;
    protected String databaseName;
    protected String collectionName;
    protected CodecRegistry pojoCodecRegistry;

    @Override
    public void service(Mail mail) throws MessagingException {
        long payloadDocumentMaxSize = 16777216;
        if (mail.getMessageSize() < payloadDocumentMaxSize) {
            var optionalMimeMessage = Optional.ofNullable(mail.getMessage());
            optionalMimeMessage.ifPresent(processMimeMessage());
        }
    }

    @Override
    public String getMailetInfo() {
        return this.getClass().getSimpleName() + " Mailet";
    }

    public Consumer<MimeMessage> processMimeMessage() {
        return mimeMessage -> {
        };
    }

    /**
     * Set up the database with init parameters from mailetcontainer.xml
     */
    protected void initParameters() {
        connectionString = getInitParameter("connectionString");
        databaseName = getInitParameter("database");
        collectionName = getInitParameter("collectionName");
        pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    }

    /**
     * Getting information about attachments as a list of MailAttachments from a Mime message
     */
    protected List<MailAttachment> getAttachments(Message message) throws IOException, MessagingException {
        var attachments = new ArrayList<MailAttachment>();
        var content = message.getContent();
        if (content instanceof Multipart) {
            var multiPart = (Multipart) content;
            for (int partCount = 0; partCount < multiPart.getCount(); partCount++) {
                var part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    var mailAttachment = getAttachment(partCount, part);
                    attachments.add(mailAttachment);
                }
            }
        }
        return attachments;
    }

    protected MailSource getMailSource(MimeMessage mimeMessage) throws MessagingException,
            IOException {
        var out = new ByteArrayOutputStream();
        mimeMessage.writeTo(out);
        return MailSource.builder()
                .id(new ObjectId())
                .msgid(mimeMessage.getMessageID())
                .data(out.toString())
                .attachments(getAttachments(mimeMessage))
                .createdAt(LocalDateTime.now())
                .build();
    }

    protected static MailAttachment getAttachment(int partCount, MimeBodyPart part) throws MessagingException {
        return MailAttachment.builder()
                .id(new ObjectId())
                .filename(part.getFileName())
                .contentType(getShortContentType(part.getContentType()))
                .disposition(part.getDisposition())
                .transferEncoding(part.getEncoding())
                .size(part.getSize())
                .partId(partCount)
                .build();
    }

    protected static String getShortContentType(String contentType) {
        var items = contentType.split(";");
        return items[0];
    }

}