package com.disposableemail.core.exception.custom;

public class MailboxNotFoundException extends IllegalArgumentException {

    public MailboxNotFoundException() {
        super("Mailbox not found");
    }
}
