package com.disposableemail.core.exception.custom;

import com.disposableemail.core.model.Credentials;

public class AccountDataBaseRegistrationException extends AccountRegistrationException {

    public AccountDataBaseRegistrationException(Credentials credentials) {
        super(credentials);
    }

}
