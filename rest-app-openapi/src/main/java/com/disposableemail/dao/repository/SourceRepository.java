package com.disposableemail.dao.repository;

import com.disposableemail.dao.entity.SourceEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SourceRepository extends ReactiveMongoRepository<SourceEntity, String> {
    Mono<SourceEntity> findByMsgid(String msgid);
}
