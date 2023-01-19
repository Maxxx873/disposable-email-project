package com.disposableemail.dao.repository.search;

import com.disposableemail.dao.entity.search.MessageElasticsearchEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MessageElasticsearchRepository extends ReactiveElasticsearchRepository<MessageElasticsearchEntity, String> {

    @Query("""
            {
              "bool": {
                "must": [
                  {
                    "term": {
                      "to.address.raw": "?0"
                    }
                  },
                  {
                    "match": {
                      "messageId": "?1"
                    }
                  }
                ]
              }
            }
            """)
    Mono<MessageElasticsearchEntity> findByAddressToAndMessageId(String addressTo, String messageId);

/*    @Query("""
            {
                "match": {
                    "mailboxId": "?0"
                }
            }
            """)*/
    Flux<MessageElasticsearchEntity> findByMailboxId(String mailboxId, Pageable pageable);


    @Query("""
            {
                "match": {
                    "mailboxId": "?0"
                }
            }
            """)
    Flux<MessageElasticsearchEntity> findByMailboxId(String mailboxId);


}
