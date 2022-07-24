package com.disposableemail.exception;

public class AccountNotFoundException extends IllegalArgumentException{

    public AccountNotFoundException() {
        super("This Account not found");
    }
}
