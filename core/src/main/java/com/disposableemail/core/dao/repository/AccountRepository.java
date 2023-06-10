package com.disposableemail.core.dao.repository;

import com.disposableemail.core.dao.entity.AccountEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends ReactiveMongoRepository<AccountEntity, String> {
    Mono<AccountEntity> findByAddress(String address);

    Flux<AccountEntity> findByIdNotNullOrderByCreatedAtDesc();

}
