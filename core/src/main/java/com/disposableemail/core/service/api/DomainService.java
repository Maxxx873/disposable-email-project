package com.disposableemail.core.service.api;

import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.model.DomainItem;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides methods for managing domains in the system.
 */
@Validated
public interface DomainService {

    /**
     * Retrieves a list of domains, excluding the "localhost" domain.
     *
     * @param size the maximum number of domains to retrieve
     * @return a flux of domain entities
     */
    Flux<DomainEntity> getDomainsExcludingLocalhost(@Min(1) Integer size);

    /**
     * Retrieves a list of all domains.
     *
     * @return a flux of domain entities
     */
    Flux<DomainEntity> getAllDomains();

    /**
     * Retrieves a domain by its ID.
     *
     * @param id the ID of the domain to retrieve
     * @return a mono of domain entity
     */
    Mono<DomainEntity> getDomain(String id);

    /**
     * Creates a new domain with the specified name.
     *
     * @param domainName the name of the domain to create
     * @return a mono that completes when the domain is created
     */
    Mono<DomainEntity> createDomain(DomainItem domainName);

    /**
     * Deletes the domain with the specified ID.
     *
     * @param id the ID of the domain to delete
     * @return a mono that completes when the domain is deleted
     */
    Mono<Void> deleteDomain(String id);

    /**
     * Updates the specified domain entity.
     *
     * @param domainEntity the domain entity to update
     * @return a mono that completes when the domain is updated
     */
    Mono<DomainEntity> updateDomain(DomainEntity domainEntity);

    /**
     * Retrieves a list of all domains.
     *
     * @return a flux of domain entities
     */
    Flux<DomainEntity> getDomains();
}

