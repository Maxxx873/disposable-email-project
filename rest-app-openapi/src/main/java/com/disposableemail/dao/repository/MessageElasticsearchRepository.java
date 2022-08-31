package com.disposableemail.dao.repository;

import com.disposableemail.dao.entity.MessageElasticsearchEntity;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MessageElasticsearchRepository extends ReactiveElasticsearchRepository<MessageElasticsearchEntity, String> {
    @Query("{\"match\": {\"to.address.raw\": \"?0\"}}")
    Flux<MessageElasticsearchEntity> findByAddressTo(String addressTo);

    @Query("{\"bool\": {\"must\": [{ \"term\":{ \"to.address.raw\": \"?0\"}},{ \"match\": { \"messageId\": \"?1\"}}]}}")
    Mono<MessageElasticsearchEntity> findByAddressToAndMessageId(String addressTo, String messageId);


}
