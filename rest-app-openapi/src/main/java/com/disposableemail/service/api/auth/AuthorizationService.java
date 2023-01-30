package com.disposableemail.service.api.auth;


import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;

import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;

public interface AuthorizationService {

    CompletableFuture<Response> createUser(Credentials credentials);

    Token getToken(Credentials credentials);
}
