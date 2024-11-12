package com.disposableemail.core.service.impl;

import static com.disposableemail.core.event.Event.Type.DOMAIN_CREATED;
import static com.disposableemail.core.event.Event.Type.DOMAIN_DELETED;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
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
@ConditionalOnProperty(name = "cache.service", havingValue = "none", matchIfMissing = true)
public class DomainServiceImpl implements DomainService {

    private final DomainRepository domainRepository;
    private final DomainMapper domainMapper;
    private final EventProducer eventProducer;

    @Override
    public Flux<DomainEntity> getDomainsExcludingLocalhost(Integer size) {
        log.info("Getting a Domains collection | Size {}", size);

        return domainRepository.findAllExcludingLocalhostOrderedByCreatedAtDesc(PageRequest.ofSize(size));
    }

    @Override
    public Flux<DomainEntity> getAllDomains() {
        log.info("Getting all Domains");

        return domainRepository.findAll();
    }

    @Override
    public Mono<DomainEntity> getDomain(String id) {
        log.info("Getting a Domain {}", id);

        return domainRepository.findById(id);
    }

    @Override
    public Mono<DomainEntity> getByDomain(String domain) {
        log.info("Getting a Domain {}", domain);

        return domainRepository.findByDomain(domain);
    }

    @Override
    public Mono<DomainEntity> createDomain(DomainItem domainItem) {
        log.info("Creating a Domain {}", domainItem.getDomain());

        return domainRepository.save(domainMapper.domainItemToDomainEntity(domainItem))
                .doOnSuccess(action -> eventProducer.send(new Event<>(DOMAIN_CREATED, domainItem.getDomain())));
    }

    @Override
    public Mono<DomainEntity> updateDomain(DomainEntity domainEntity) {
        log.info("Updating a Domain {}", domainEntity.getDomain());

        return domainRepository.save(domainEntity);
    }

    @Override
    public Mono<Void> deleteDomain(String id) {
        log.info("Deleting a Domain | id: {}", id);

        return domainRepository.findById(id)
                .switchIfEmpty(Mono.error(new DomainNotFoundException()))
                .flatMap(domainEntity ->
                        domainRepository.deleteById(id)
                                .doOnSuccess(action -> {
                                    log.info("Domain {} is deleted", id);
                                    eventProducer.send(new Event<>(DOMAIN_DELETED, domainEntity.getDomain()));
                                }));
    }

}
