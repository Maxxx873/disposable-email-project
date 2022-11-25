package com.disposableemail.apache.james.matcher;

import com.google.common.collect.ImmutableList;
import org.apache.james.core.MailAddress;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.util.Collection;

public class NotBigMessage extends GenericMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotBigMessage.class);

    private final long PAYLOAD_DOCUMENT_MAX_SIZE = 16777216;

    @Override
    public Collection<MailAddress> match(Mail mail) throws MessagingException {

        if (mail.getMessageSize() < PAYLOAD_DOCUMENT_MAX_SIZE) {
            LOGGER.info("Message {} size is {} - is not a Big message", mail.getName(), mail.getMessageSize());
            return ImmutableList.copyOf(mail.getRecipients());
        } else {
            LOGGER.warn("Message {} size is {} - is a Big message", mail.getName(), mail.getMessageSize());
            return ImmutableList.of();
        }
    }
}
