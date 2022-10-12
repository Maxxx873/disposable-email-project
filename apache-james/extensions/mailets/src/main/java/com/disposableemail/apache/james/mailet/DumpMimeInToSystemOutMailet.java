package com.disposableemail.apache.james.mailet;

import lombok.extern.slf4j.Slf4j;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMailet;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

/**
 * A Mailet for dump Mime messages to System.out.
 */
@Slf4j
public class DumpMimeInToSystemOutMailet extends GenericMailet {

    @Override
    public String getMailetInfo() {
        return "Dump Mime message to System.out";
    }

    @Override
    public void init() throws MessagingException {
        super.init();
        System.out.println("---[Dump Mime message to System.out Mailet]---");
    }

    @Override
    public void service(Mail mail) throws MessagingException {
        try {
            System.out.println("---[Mime - Message]---");
            MimeMessage message = mail.getMessage();
            message.writeTo(System.out);
            System.out.println("------------");
        } catch (IOException e) {
            log.error("error printing message", e);
        }
    }

}