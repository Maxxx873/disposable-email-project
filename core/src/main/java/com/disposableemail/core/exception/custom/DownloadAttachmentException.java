package com.disposableemail.core.exception.custom;

public class DownloadAttachmentException extends RuntimeException {

    public DownloadAttachmentException() {
        this("Download attachment error");
    }

    public DownloadAttachmentException(String message) {
        super(message);
    }
}