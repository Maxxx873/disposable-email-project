package com.disposableemail.service.api;

import com.disposableemail.rest.model.Credentials;

public interface EmailService {

    void createUser(Credentials credentials);
}
