package com.disposableemail.core.exception.custom;

import com.disposableemail.core.model.Credentials;

public class AccountAuthServerRegistrationException extends AccountRegistrationException {

    public AccountAuthServerRegistrationException(Credentials credentials) {
        super(credentials);
    }

}
