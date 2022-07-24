package com.disposableemail.service.api;

import com.disposableemail.dao.entity.DomainEntity;
import reactor.core.publisher.Flux;

public interface DomainService {

    Flux<DomainEntity> getMockDomains(Integer page);

}
