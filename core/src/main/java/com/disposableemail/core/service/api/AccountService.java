package com.disposableemail.core.service.api;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.model.Credentials;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * Provides methods for managing accounts in a system.
 */
public interface AccountService {

    /**
     * Creates a new account with the given credentials.
     *
     * @param credentials the credentials for the new account
     * @return a Mono that emits the newly created account entity
     */
    Mono<AccountEntity> createAccount(Credentials credentials);

    /**
     * Retrieves an account by its ID.
     *
     * @param id the ID of the account to retrieve
     * @return a Mono that emits the account entity with the given ID, or an empty Mono if no account was found
     */
    Mono<AccountEntity> getAccountById(String id);

    /**
     * Retrieves an account by its address.
     *
     * @param address the address of the account to retrieve
     * @return a Mono that emits the account entity with the given address, or an empty Mono if no account was found
     */
    Mono<AccountEntity> getAccountByAddress(String address);

    /**
     * Deletes an account by its ID.
     *
     * @param id the ID of the account to delete
     * @return a Mono that emits the deleted account entity, or an empty Mono if no account was found
     */
    Mono<AccountEntity> deleteAccount(String id);

    /**
     * Soft deletes an account by its ID.
     *
     * @param id the ID of the account to softly delete
     * @return a Mono that emits the soft-deleted account entity, or an empty Mono if no account was found
     */
    Mono<AccountEntity> softDeleteAccount(String id);

    /**
     * Retrieves a list of accounts with pagination.
     *
     * @param size the number of accounts to retrieve
     * @param offset the offset of the first account to retrieve
     * @return a Flux that emits the account entities with the given size and offset
     */
    Flux<AccountEntity> getAccounts(int size, int offset);
}