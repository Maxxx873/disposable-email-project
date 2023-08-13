package com.disposableemail.core.exception.custom;

public class AccountToManyRequestsException extends IllegalArgumentException {

    public AccountToManyRequestsException(String message) {
        super(message + " not registered - to many requests");
    }

    public AccountToManyRequestsException() {
        super("This account not registered - to many requests");
    }

}
