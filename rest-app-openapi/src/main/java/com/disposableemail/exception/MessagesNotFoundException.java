package com.disposableemail.exception;

public class MessagesNotFoundException extends IllegalArgumentException {

    public MessagesNotFoundException() {
        super("Messages not found");
    }
}
