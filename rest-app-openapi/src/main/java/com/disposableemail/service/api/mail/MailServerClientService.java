package com.disposableemail.service.api.mail;

import com.disposableemail.dao.entity.DomainEntity;
import com.disposableemail.rest.model.Credentials;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.ws.rs.core.Response;

public interface MailServerClientService {

    Flux<DomainEntity> getDomains();

    Mono<Response> createUser(Credentials credentials);

    Mono<String> getMailboxId(String username, String mailboxName);

    Mono<Response> createMailbox(Credentials credentials, String mailboxName);

    Mono<Response> getQuotaSize(Credentials credentials);

    Mono<Response> updateQuotaSize(Credentials credentials);

}