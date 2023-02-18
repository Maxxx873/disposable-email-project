package com.disposableemail.core.exception.custom;

public class MessagesNotFoundException extends IllegalArgumentException {

    public MessagesNotFoundException() {
        super("Messages not found");
    }
}
