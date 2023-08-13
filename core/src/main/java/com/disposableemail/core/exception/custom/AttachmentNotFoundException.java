package com.disposableemail.core.exception.custom;

public class AttachmentNotFoundException extends IllegalArgumentException {

    public AttachmentNotFoundException(String message) {
        super("This Message not found");
    }

    public AttachmentNotFoundException() {
        super("This Message not found");
    }
}
