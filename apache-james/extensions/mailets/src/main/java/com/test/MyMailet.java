package com.test;

import javax.mail.MessagingException;

import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMailet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyMailet extends GenericMailet{
    private static final Logger logger = LoggerFactory.getLogger(MyMailet.class);

    @Override
    public void init() {
        System.out.println("Myy Mailet Init !!!");
        logger.info("Myy Mailet Init !!!");

    }
    @Override
    public void service(Mail mail) throws MessagingException {
        logger.info("Service");
    }
}