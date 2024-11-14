package com.disposableemail.core.service.impl;

import static com.disposableemail.core.event.Event.Type.DOMAIN_CREATED;
import static com.disposableemail.core.event.Event.Type.DOMAIN_DELETED;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;

import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.dao.mapper.DomainMapper;
import com.disposableemail.core.dao.repository.DomainRepository;
import com.disposableemail.core.event.Event;
import com.disposableemail.core.event.producer.EventProducer;
import com.disposableemail.core.exception.custom.DomainNotFoundException;
import com.disposableemail.core.model.DomainItem;
import com.disposableemail.core.service.api.DomainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cache.service", havingValue = "redis")
public class DomainServiceRedisImpl implements DomainService {

    public static final String DOMAIN_NAME_KEY_PREFIX = "domain_name_";
    public static final String DOMAIN_ID_KEY_PREFIX = "domain_id_";
    private final DomainRepository domainRepository;
    private final DomainMapper domainMapper;
    private final EventProducer eventProducer;
    private final ReactiveRedisOperations<String, Object> redisOps;

    @Override
    public Flux<DomainEntity> getDomainsExcludingLocalhost(Integer size) {
        log.info("Getting a Domains collection | Size {}", size);

        return redisOps.opsForList().range(DOMAIN_NAME_KEY_PREFIX + "*", 0, size)
                .cast(DomainEntity.class)
                .switchIfEmpty(domainRepository.findAllExcludingLocalhostOrderedByCreatedAtDesc(PageRequest.ofSize(size))
                        .flatMap(this::updateDomainInRedisCache));
    }

    @Override
    public Flux<DomainEntity> getAllDomains() {
        log.info("Getting all Domains");

        return redisOps.keys(DOMAIN_NAME_KEY_PREFIX + "*")
                .flatMap(redisOps.opsForValue()::get)
                .cast(DomainEntity.class)
                .switchIfEmpty(domainRepository.findAll()
                        .flatMap(this::updateDomainByIdInRedisCache));
    }

    @Override
    public Mono<DomainEntity> getDomain(String id) {
        log.info("Getting a Domain by id: {}", id);

        return redisOps.opsForValue().get(id)
                .switchIfEmpty(domainRepository.findById(id))
                .cast(DomainEntity.class)
                .flatMap(this::updateDomainByIdInRedisCache);
    }

    @Override
    public Mono<DomainEntity> getByDomain(String domain) {
        log.info("Getting a Domain {}", domain);

        return redisOps.opsForValue().get(domain)
                .switchIfEmpty(domainRepository.findByDomain(domain))
                .cast(DomainEntity.class)
                .flatMap(this::updateDomainInRedisCache);
    }

    @Override
    public Mono<DomainEntity> createDomain(DomainItem domainItem) {
        log.info("Creating a Domain {}", domainItem.getDomain());

        return domainRepository.save(domainMapper.domainItemToDomainEntity(domainItem))
                .flatMap(this::updateDomainInRedisCache)
                .doOnSuccess(action -> eventProducer.send(new Event<>(DOMAIN_CREATED, domainItem.getDomain())));
    }

    @Override
    public Mono<DomainEntity> updateDomain(DomainEntity domainEntity) {
        log.info("Updating a Domain {}", domainEntity.getDomain());

        return domainRepository.save(domainEntity)
                .flatMap(this::updateDomainInRedisCache);
    }

    @Override
    public Mono<Void> deleteDomain(String id) {
        log.info("Deleting a Domain | id: {}", id);

        return domainRepository.findById(id)
                .switchIfEmpty(Mono.error(new DomainNotFoundException()))
                .flatMap(this::deleteDomainFromRedisCache)
                .flatMap(domainEntity -> domainRepository.deleteById(id)
                        .doOnSuccess(action -> sendDeletedDomainEvent(id, domainEntity)));
    }

    private void sendDeletedDomainEvent(String id, DomainEntity domainEntity) {
        log.info("Domain {} is deleted", id);
        eventProducer.send(new Event<>(DOMAIN_DELETED, domainEntity.getDomain()));
    }

    private Mono<DomainEntity> updateDomainInRedisCache(DomainEntity domainEntity) {
        return redisOps.opsForValue().set(DOMAIN_NAME_KEY_PREFIX + domainEntity.getDomain(), domainEntity)
                .thenReturn(domainEntity);
    }

    private Mono<DomainEntity> updateDomainByIdInRedisCache(DomainEntity domainEntity) {
        return redisOps.opsForValue().set(DOMAIN_ID_KEY_PREFIX + domainEntity.getId(), domainEntity)
                .thenReturn(domainEntity);
    }

    private Mono<DomainEntity> deleteDomainFromRedisCache(DomainEntity domainEntity) {
        return redisOps.opsForValue().delete(DOMAIN_NAME_KEY_PREFIX + domainEntity.getDomain())
                .thenReturn(domainEntity);
    }

}
