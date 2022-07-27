package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.DomainEntity;
import com.disposableemail.dao.repository.DomainRepository;
import com.disposableemail.service.api.DomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainServiceImpl implements DomainService {

    private final DomainRepository domainRepository;

    @Override
    public Flux<DomainEntity> getMockDomains(Integer size) {

        var domainList = Arrays.asList(
                DomainEntity.builder()
                        .isPrivate(false)
                        .isActive(true)
                        .domain("example.com")
                        .build(),
                DomainEntity.builder()
                        .isPrivate(true)
                        .isActive(true)
                        .domain("example.org")
                        .build(),
                DomainEntity.builder()
                        .isPrivate(true)
                        .isActive(false)
                        .domain("example.xyz")
                        .build(),
                DomainEntity.builder()
                        .isPrivate(false)
                        .isActive(true)
                        .domain("example.biz")
                        .build(),
                DomainEntity.builder()
                        .isPrivate(false)
                        .isActive(true)
                        .domain("example.usa")
                        .build()
        );

        return domainRepository.deleteAll()
                .thenMany(Flux
                        .just(domainList)
                        .flatMap(domainRepository::saveAll))
                .thenMany(domainRepository.findByIdNotNullOrderByCreatedAtDesc(PageRequest.ofSize(size)));
    }

}
