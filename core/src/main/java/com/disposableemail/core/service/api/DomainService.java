package com.disposableemail.core.service.api;

import com.disposableemail.core.dao.entity.DomainEntity;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
public interface DomainService {

    Flux<DomainEntity> getDomainsFromEmailServerAndSaveToDb(@Min(1) Integer size);

    Mono<DomainEntity> getDomain(String id);
}
