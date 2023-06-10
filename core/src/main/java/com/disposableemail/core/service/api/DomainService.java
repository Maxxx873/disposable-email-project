package com.disposableemail.core.service.api;

import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.model.DomainItem;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
public interface DomainService {

    Flux<DomainEntity> getDomainsExcludingLocalhost(@Min(1) Integer size);

    Mono<DomainEntity> getDomain(String id);

    Mono<DomainEntity> createDomain(DomainItem domainName);

    Mono<Void> deleteDomain(String id);

    Mono<DomainEntity> updateDomain(DomainEntity domainEntity);

    Flux<DomainEntity> getDomains();

}
