package com.disposableemail.core.exception.custom;

public class MessageNotFoundException extends IllegalArgumentException {

    public MessageNotFoundException() {
        super("This Message not found");
    }
}
