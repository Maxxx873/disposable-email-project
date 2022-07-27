package com.disposableemail.exception;

public class DomainNotAvailableException extends IllegalArgumentException{

    public DomainNotAvailableException() {
        super("This Domain is not available");
    }
}
