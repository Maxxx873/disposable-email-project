package com.disposableemail.core.exception.custom;

import com.disposableemail.core.model.Credentials;

public class AccountMailServerRegistrationException extends AccountRegistrationException {

    public AccountMailServerRegistrationException(Credentials credentials) {
        super(credentials);
    }

}
