package com.disposableemail.service.api;


import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;

public interface AuthorizationService {
    String createUser(Credentials credentials);
    Token getToken(Credentials credentials);
}
