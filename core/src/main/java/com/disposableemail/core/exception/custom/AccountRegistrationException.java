package com.disposableemail.core.exception.custom;

import com.disposableemail.core.model.Credentials;
import lombok.Getter;

@Getter
public class AccountRegistrationException extends IllegalArgumentException {
    private Credentials credentials;

    public AccountRegistrationException() {
        super("Error in the account registration process");
    }

    public AccountRegistrationException(Credentials credentials) {
        super("Error in the account " + credentials.getAddress() + " registration process");
        this.credentials = credentials;
    }

}
