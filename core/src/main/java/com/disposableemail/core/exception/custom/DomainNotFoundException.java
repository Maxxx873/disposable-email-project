package com.disposableemail.core.exception.custom;

public class DomainNotFoundException extends IllegalArgumentException {

    public DomainNotFoundException() {
        super("This Domain not found");
    }
}
