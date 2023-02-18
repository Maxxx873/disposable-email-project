package com.disposableemail.core.service.impl;

import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.dao.repository.DomainRepository;
import com.disposableemail.core.service.api.DomainService;
import com.disposableemail.core.service.api.mail.MailServerClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainServiceImpl implements DomainService {

    @Value("${mail-server.name}")
    private String mailServerName;

    private final DomainRepository domainRepository;
    private final MailServerClientService mailServerClientService;

    @Override
    public Flux<DomainEntity> getDomainsFromEmailServerAndSaveToDb(Integer size) {
        log.info("Getting a Domains collection | Size {}", size);

        return mailServerClientService.getDomains()
                .flatMap(domainEntity -> domainRepository.findByDomain(domainEntity.getDomain()).switchIfEmpty(Mono.defer(() -> {
                    log.info("Added a new Domain from {} | {} ", mailServerName, domainEntity.getDomain());
                    return domainRepository.save(domainEntity);
                })))
                .thenMany(domainRepository.findByIdNotNullOrderByCreatedAtDesc(PageRequest.ofSize(size)));
    }

    @Override
    public Mono<DomainEntity> getDomain(String id) {
        log.info("Getting a Domain {}", id);

        return domainRepository.findById(id);
    }
}
