package com.disposableemail.core.exception.custom;

public class SourceNotFoundException extends IllegalArgumentException {

    public SourceNotFoundException() {
        super("This Source not found");
    }
}
