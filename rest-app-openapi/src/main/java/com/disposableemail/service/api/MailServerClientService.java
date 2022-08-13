package com.disposableemail.service.api;

import com.disposableemail.dao.entity.DomainEntity;
import com.disposableemail.rest.model.Credentials;
import reactor.core.publisher.Flux;

import javax.ws.rs.core.Response;

public interface MailServerClientService {

    Flux<DomainEntity> getDomains();

    Response createUser(Credentials credentials);

}
