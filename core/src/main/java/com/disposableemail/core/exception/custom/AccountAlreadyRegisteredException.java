package com.disposableemail.core.exception.custom;

public class AccountAlreadyRegisteredException extends IllegalArgumentException {

    public AccountAlreadyRegisteredException() {
        super("This account is already registered");
    }

    public AccountAlreadyRegisteredException(String message) {
        super("Account " + message + " is already registered");
    }

}
