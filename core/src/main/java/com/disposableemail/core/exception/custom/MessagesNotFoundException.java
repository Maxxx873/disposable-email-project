package com.disposableemail.core.exception.custom;

public class MessagesNotFoundException extends IllegalArgumentException {

    public MessagesNotFoundException() {
        super("No messages found");
    }
}
