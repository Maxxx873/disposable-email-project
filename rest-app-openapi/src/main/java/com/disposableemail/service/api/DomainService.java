package com.disposableemail.service.api;

import com.disposableemail.dao.entity.DomainEntity;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;

import javax.validation.constraints.Min;

@Validated
public interface DomainService {

    Flux<DomainEntity> getMockDomains(@Min(1) Integer page);

}
