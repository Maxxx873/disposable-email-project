package com.disposableemail.exception;

public class MailServerConnectException extends RuntimeException {

    public MailServerConnectException() {
        super("Mail Server connection refused");
    }
}
