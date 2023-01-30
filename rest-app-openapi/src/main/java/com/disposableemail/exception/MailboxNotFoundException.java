package com.disposableemail.exception;

public class MailboxNotFoundException extends IllegalArgumentException {

    public MailboxNotFoundException() {
        super("Mailbox not found");
    }
}
