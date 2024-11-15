package com.disposableemail.core.dao.repository;

import com.disposableemail.core.dao.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Meta;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface MessageRepository extends ReactiveMongoRepository<MessageEntity, String> {

    @Meta(cursorBatchSize = 500)
    Flux<MessageEntity> findByAccountIdAndIsDeletedFalseOrderByCreatedAtDesc(String accountId, Pageable pageable);

    Mono<MessageEntity> findByIdAndAccountIdAndIsDeletedFalse(String messageId, String accountId);

    Mono<Void> deleteByCreatedAtBefore(Instant cutoffDate);

}
