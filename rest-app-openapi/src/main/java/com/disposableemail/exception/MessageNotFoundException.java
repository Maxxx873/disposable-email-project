package com.disposableemail.exception;

public class MessageNotFoundException extends IllegalArgumentException{

    public MessageNotFoundException() {
        super("This Message not found");
    }
}
