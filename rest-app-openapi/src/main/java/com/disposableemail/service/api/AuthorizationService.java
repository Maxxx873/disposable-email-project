package com.disposableemail.service.api;


import com.disposableemail.rest.model.Credentials;

public interface AuthorizationService {
    String createUser(Credentials credentials);
    String getToken(Credentials credentials);
}
