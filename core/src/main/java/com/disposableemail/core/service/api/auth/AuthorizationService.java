package com.disposableemail.core.service.api.auth;


import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.model.Token;

import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;

public interface AuthorizationService {

    CompletableFuture<Response> createUser(Credentials credentials);

    CompletableFuture<Response> deleteUser(String username);

    Token getToken(Credentials credentials);
}
