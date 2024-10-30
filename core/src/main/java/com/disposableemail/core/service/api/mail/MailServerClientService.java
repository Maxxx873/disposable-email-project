package com.disposableemail.core.service.api.mail;

import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.model.Credentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.core.Response;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface MailServerClientService {

    Mono<Response> createDomain(String domain);

    Mono<Response> deleteDomain(String domain);

    Flux<DomainEntity> getDomains();

    Mono<Response> createUserWithCredsDecrypt(Credentials credentials) throws JsonProcessingException;

    Mono<String> getMailboxId(Credentials credentials, String mailboxName);

    Mono<ResponseEntity<Void>> createUser(Credentials credentials) throws JsonProcessingException;

    Mono<Response>  deleteUser(String username);

    Mono<ResponseEntity<Void>> createMailbox(Credentials credentials);

    Mono<Response> createMailboxOld(Credentials credentials);

    Mono<Integer> getQuotaSize(String username);

    Mono<ResponseEntity<Void>> updateQuotaSize(Credentials credentials);

    Mono<Integer> getUsedSize(String username);
}