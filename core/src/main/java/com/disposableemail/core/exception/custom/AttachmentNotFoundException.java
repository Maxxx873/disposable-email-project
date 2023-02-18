package com.disposableemail.core.exception.custom;

public class AttachmentNotFoundException extends IllegalArgumentException {

    public AttachmentNotFoundException() {
        super("This Message not found");
    }
}
