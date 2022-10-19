package com.disposableemail.service.api;

import com.disposableemail.dao.entity.DomainEntity;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Min;

@Validated
public interface DomainService {

    Flux<DomainEntity> getDomainsFromEmailServerAndSaveToDb(@Min(1) Integer size);

    Mono<DomainEntity> getDomain(String id);
}
