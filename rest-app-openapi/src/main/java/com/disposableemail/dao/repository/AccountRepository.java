package com.disposableemail.dao.repository;

import com.disposableemail.dao.entity.AccountEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends ReactiveMongoRepository<AccountEntity, String> {
}
