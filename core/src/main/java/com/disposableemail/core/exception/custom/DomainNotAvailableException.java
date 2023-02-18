package com.disposableemail.core.exception.custom;

public class DomainNotAvailableException extends IllegalArgumentException {

    public DomainNotAvailableException() {
        super("This Domain is not available");
    }
}
