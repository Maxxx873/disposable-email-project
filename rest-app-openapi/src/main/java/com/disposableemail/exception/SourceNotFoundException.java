package com.disposableemail.exception;

public class SourceNotFoundException extends IllegalArgumentException{

    public SourceNotFoundException() {
        super("This Source not found");
    }
}
