package com.disposableemail.service.api.auth;


import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;

import javax.ws.rs.core.Response;

public interface AuthorizationService {

    Response createUser(Credentials credentials);

    Token getToken(Credentials credentials);
}
