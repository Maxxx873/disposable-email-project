package com.disposableemail.apache.james.matcher;

import com.google.common.collect.ImmutableList;
import org.apache.james.core.MailAddress;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMatcher;

import javax.mail.MessagingException;
import java.util.Collection;

public class NotBigMessage extends GenericMatcher {

    private final long PAYLOAD_DOCUMENT_MAX_SIZE = 16777216;

    @Override
    public Collection<MailAddress> match(Mail mail) throws MessagingException {

        if (mail.getMessageSize() < PAYLOAD_DOCUMENT_MAX_SIZE) {
            System.out.printf("Message %s size is %s - is not a Big message",
                    mail.getName(), mail.getMessageSize());
            return ImmutableList.copyOf(mail.getRecipients());
        } else {
            System.out.printf("Message %s size is %s - is a Big message",
                    mail.getName(), mail.getMessageSize());
            return ImmutableList.of();
        }
    }
}
