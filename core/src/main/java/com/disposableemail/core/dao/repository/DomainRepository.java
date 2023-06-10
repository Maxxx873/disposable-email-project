package com.disposableemail.core.dao.repository;

import com.disposableemail.core.dao.entity.DomainEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@Repository
public interface DomainRepository extends ReactiveMongoRepository<DomainEntity, String> {

    Flux<DomainEntity> findByIdNotNullOrderByCreatedAtDesc(Pageable pageable);

    Mono<DomainEntity> findByDomain(String domainName);

    @Query(value = "{ 'domain':  { $not: { $eq: 'localhost' } } }", sort = "{ 'createdAt': -1 }")
    Flux<DomainEntity> findAllExcludingLocalhostOrderedByCreatedAtDesc(Pageable pageable);

}
