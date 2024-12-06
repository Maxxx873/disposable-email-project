package com.disposableemail.core.exception.custom;

public class MailServerUpdateUsedSizeException extends RuntimeException {

    public MailServerUpdateUsedSizeException() {
        super("Mail Server usedSize not updated");
    }
}
