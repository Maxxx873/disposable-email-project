package com.disposableemail.dao.repository;

import com.disposableemail.dao.entity.MessageEntity;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MessageRepository extends ReactiveElasticsearchRepository<MessageEntity, String> {
    @Query("{\"match\": {\"to.address.raw\": \"?0\"}}")
    Flux<MessageEntity> findByAddressTo(String addressTo);

    @Query("{\"bool\": {\"must\": [{ \"match\":{ \"to.address.raw\": \"?0\"}},{ \"match\": { \"messageId\": \"?1\"}}]}}")
    Mono<MessageEntity> findByAddressToAndMessageId(String addressTo, String messageId);

}
