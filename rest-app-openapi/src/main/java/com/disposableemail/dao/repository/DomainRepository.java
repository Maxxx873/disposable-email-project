package com.disposableemail.dao.repository;

import com.disposableemail.dao.entity.DomainEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DomainRepository extends ReactiveMongoRepository<DomainEntity, String> {

    Flux<DomainEntity> findByIdNotNullOrderByCreatedAtDesc(Pageable pageable);
}
