package com.disposableemail.core.service.impl;

import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.dao.mapper.DomainMapper;
import com.disposableemail.core.dao.repository.DomainRepository;
import com.disposableemail.core.event.Event;
import com.disposableemail.core.event.EventProducer;
import com.disposableemail.core.exception.custom.DomainNotFoundException;
import com.disposableemail.core.model.DomainItem;
import com.disposableemail.core.service.api.DomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.disposableemail.core.event.Event.Type.DOMAIN_CREATED;
import static com.disposableemail.core.event.Event.Type.DOMAIN_DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
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
    public Mono<DomainEntity> getDomain(String id) {
        log.info("Getting a Domain {}", id);

        return domainRepository.findById(id);
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

    @Override
    public Flux<DomainEntity> getDomains() {
        log.info("Getting a Domains collection");

        return domainRepository.findAll();
    }
}
