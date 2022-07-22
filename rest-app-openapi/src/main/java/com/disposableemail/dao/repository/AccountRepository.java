package com.disposableemail.dao.repository;

import com.disposableemail.dao.entity.AccountEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends ReactiveMongoRepository<AccountEntity, String> {

    Mono<AccountEntity> findByAddress(String address);
}
