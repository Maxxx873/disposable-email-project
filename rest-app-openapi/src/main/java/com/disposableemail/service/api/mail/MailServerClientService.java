package com.disposableemail.service.api.mail;

import com.disposableemail.dao.entity.DomainEntity;
import com.disposableemail.rest.model.Credentials;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.ws.rs.core.Response;

public interface MailServerClientService {

    Flux<DomainEntity> getDomains();

    Mono<Response> createUser(Credentials credentials);

    Mono<String> getMailboxId(Credentials credentials, String mailboxName);

    Mono<Response> createMailbox(Credentials credentials);

    Mono<Integer> getQuotaSize(String username);

    Mono<Response> updateQuotaSize(Credentials credentials);

    Mono<Integer> getUsedSize(String username);

}