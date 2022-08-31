package com.disposableemail.dao.repository;

import com.disposableemail.dao.entity.MessageEntity;
import com.disposableemail.rest.model.Address;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface MessageRepository extends ReactiveMongoRepository<MessageEntity, String> {

    Flux<MessageEntity> findByToAndIsDeletedFalseOrderByCreatedAtDesc(List<Address> to, Pageable pageable);

    Mono<MessageEntity> findByIdAndAccountIdAndIsDeletedFalse(String messageId, String accountId);
}
