package com.disposableemail.exception;

public class AttachmentNotFoundException extends IllegalArgumentException {

    public AttachmentNotFoundException() {
        super("This Message not found");
    }
}
