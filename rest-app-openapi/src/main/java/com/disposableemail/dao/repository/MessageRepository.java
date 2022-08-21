package com.disposableemail.dao.repository;

import com.disposableemail.dao.entity.MessageEntity;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MessageRepository extends ReactiveElasticsearchRepository<MessageEntity, String> {
    @Query("{\"match_phrase\": {\"to.address.raw\": \"?0\"}}")
    Flux<MessageEntity> findByAddressTo(String addressTo);

    Mono<MessageEntity> findByMessageId(String id);


}
