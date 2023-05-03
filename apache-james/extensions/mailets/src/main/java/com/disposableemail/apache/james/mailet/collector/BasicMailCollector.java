package com.disposableemail.apache.james.mailet.collector;

import com.disposableemail.apache.james.mailet.collector.pojo.Address;
import com.disposableemail.apache.james.mailet.collector.pojo.Attachment;
import com.disposableemail.apache.james.mailet.collector.pojo.MailMessage;
import com.disposableemail.apache.james.mailet.collector.pojo.Source;
import com.mongodb.MongoClientSettings;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMailet;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class BasicMailCollector extends GenericMailet {

    protected String connectionString;
    protected String databaseName;
    protected String sourceCollectionName;
    protected String messageCollectionName;
    protected String accountCollectionName;
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

    protected void initParameters() {
        connectionString = getInitParameter("connectionString");
        databaseName = getInitParameter("database");
        sourceCollectionName = getInitParameter("sourceCollectionName");
        messageCollectionName = getInitParameter("messageCollectionName");
        accountCollectionName = getInitParameter("accountCollectionName");
        pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    }

    protected static Source getMailSource(MimeMessage mimeMessage) throws MessagingException,
            IOException {
        var out = new ByteArrayOutputStream();
        mimeMessage.writeTo(out);
        return Source.builder()
                .id(new ObjectId())
                .msgid(mimeMessage.getMessageID())
                .data(out.toString(StandardCharsets.UTF_8))
                .attachments(getAttachments(mimeMessage))
                .createdAt(LocalDateTime.now())
                .build();
    }

    protected static List<Attachment> getAttachments(Message message) throws IOException, MessagingException {
        var attachments = new ArrayList<Attachment>();
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

    protected static Attachment getAttachment(int partCount, MimeBodyPart part) throws MessagingException {
        return Attachment.builder()
                .id(new ObjectId())
                .filename(part.getFileName())
                .contentType(getShortContentType(part.getContentType()))
                .disposition(part.getDisposition())
                .transferEncoding(part.getEncoding())
                .size(part.getSize())
                .partId(partCount)
                .build();
    }

    protected static MailMessage getMailMessage(MimeMessage mimeMessage) throws MessagingException, IOException {
        List<Attachment> attachments;
        attachments = getAttachments(mimeMessage);

        return MailMessage.builder()
                .id(new ObjectId())
                .accountId("")
                .msgid(Optional.ofNullable(mimeMessage.getMessageID()).orElse(""))
                .from(mapJavaxMailAddressToAddress(mimeMessage.getFrom()))
                .to(mapJavaxMailAddressToAddress(mimeMessage.getRecipients(Message.RecipientType.TO)))
                .cc(mapJavaxMailAddressToAddress(mimeMessage.getRecipients(Message.RecipientType.CC)))
                .bcc(mapJavaxMailAddressToAddress(mimeMessage.getRecipients(Message.RecipientType.BCC)))
                .subject(Optional.ofNullable(mimeMessage.getSubject()).orElse(""))
                .isUnread(true)
                .isFlagged(mimeMessage.getFlags().getSystemFlags().length > 0)
                .isDeleted(false)
                .text(getTextFromMessage(mimeMessage))
                .html(getHtmlParts(mimeMessage))
                .hasAttachment(!attachments.isEmpty())
                .attachments(attachments)
                .size(mimeMessage.getSize())
                .sentDate(mimeMessage.getSentDate().toInstant())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    protected static List<String> getHtmlParts(Message message) throws MessagingException, IOException {
        var content = message.getContent();
        var resultBuilder = new StringBuilder();
        if (content instanceof Multipart) {
            var mimeMultipart = (MimeMultipart) message.getContent();
            for (int partCount = 0; partCount < mimeMultipart.getCount(); partCount++) {
                var bodyPart = mimeMultipart.getBodyPart(partCount);
                if (bodyPart.isMimeType("text/html")) {
                    var html = bodyPart.getContent().toString();
                    resultBuilder.append(Jsoup.parse(html));
                }
            }
        }
        if (resultBuilder.toString().isEmpty()) {
            return Collections.emptyList();
        }
        return List.of(resultBuilder.toString());
    }

    protected static String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else {
            var content = message.getContent();
            var resultBuilder = new StringBuilder();
            if (content instanceof Multipart) {
                var mimeMultipart = (MimeMultipart) message.getContent();
                for (int partCount = 0; partCount < mimeMultipart.getCount(); partCount++) {
                    var bodyPart = mimeMultipart.getBodyPart(partCount);
                    if (bodyPart.isMimeType("text/plain")) {
                        resultBuilder.append((bodyPart.getContent().toString()));
                    }
                }
            }
            return resultBuilder.toString();
        }
    }

    protected static String getShortContentType(String contentType) {
        return Arrays.stream(contentType.split(";")).findFirst().orElse("");
    }

    private static List<Address> mapJavaxMailAddressToAddress(javax.mail.Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.stream(addresses).map(address ->
                Address.builder()
                        .address(((InternetAddress) address).getAddress())
                        .name(((InternetAddress) address).getPersonal())
                        .build()).collect(Collectors.toList());
    }
}