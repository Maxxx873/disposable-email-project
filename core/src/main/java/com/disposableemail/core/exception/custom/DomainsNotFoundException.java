package com.disposableemail.core.exception.custom;

public class DomainsNotFoundException extends IllegalArgumentException {
    public DomainsNotFoundException() {
        super("No domains found");
    }
}
