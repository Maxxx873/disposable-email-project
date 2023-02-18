package com.disposableemail.core.exception.custom;

public class MailServerConnectException extends RuntimeException {

    public MailServerConnectException() {
        super("Mail Server connection refused");
    }
}
