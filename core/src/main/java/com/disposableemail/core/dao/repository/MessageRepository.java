package com.disposableemail.core.dao.repository;

import com.disposableemail.core.dao.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MessageRepository extends ReactiveMongoRepository<MessageEntity, String> {

    Flux<MessageEntity> findByAccountIdAndIsDeletedFalseOrderByCreatedAtDesc(String accountId, Pageable pageable);

    Mono<MessageEntity> findByIdAndAccountIdAndIsDeletedFalse(String messageId, String accountId);

}
