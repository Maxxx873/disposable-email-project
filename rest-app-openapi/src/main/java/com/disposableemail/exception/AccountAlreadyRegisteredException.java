package com.disposableemail.exception;

public class AccountAlreadyRegisteredException extends IllegalArgumentException{

    public AccountAlreadyRegisteredException() {
        super("This account is already registered");
    }
}
