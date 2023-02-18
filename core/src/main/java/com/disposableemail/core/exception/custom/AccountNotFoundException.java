package com.disposableemail.core.exception.custom;

public class AccountNotFoundException extends IllegalArgumentException {

    public AccountNotFoundException() {
        super("This Account not found");
    }
}
