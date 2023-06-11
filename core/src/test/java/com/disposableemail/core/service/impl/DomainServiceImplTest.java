package com.disposableemail.core.service.impl;

import com.disposableemail.config.TestConfig;
import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.dao.repository.DomainRepository;
import com.disposableemail.core.model.DomainItem;
import com.disposableemail.core.service.api.DomainService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
class DomainServiceImplTest {

    @Autowired
    private DomainService domainService;
    @Autowired
    private DomainRepository domainRepository;

    private final DomainEntity domain1 = new DomainEntity("1", "example.com", true, false);
    private final DomainEntity domain2 = new DomainEntity("2", "example.org", true, true);

    private final DomainEntity localhostDomain = new DomainEntity("3", "localhost", true, true);

    private final DomainItem domainItem = new DomainItem("example.xyz", true, true);



    @BeforeEach
    public void setUp() {
        domainRepository.save(domain1).block();
        domainRepository.save(domain2).block();
        domainRepository.save(localhostDomain).block();
    }

    @AfterEach
    public void cleanUp() {
        domainRepository.deleteAll().block();
    }

     @Test
    void shouldFindAllDomains() {
        var domains = domainService.getDomains();

        StepVerifier
                .create(domains)
                .expectSubscription()
                .expectNextCount(3)
                .expectComplete()
                .verify();
    }

    @Test
    void shouldGetDomainsExcludingLocalhost() {
        var domains = domainService.getDomainsExcludingLocalhost(100);

        StepVerifier
                .create(domains)
                .expectSubscription()
                .assertNext(domain -> {
                    assertEquals(domain1.getDomain(), domain.getDomain());
                    assertThat(domain1.getIsActive()).isTrue();
                    assertThat(domain1.getIsPrivate()).isFalse();
                    assertNotNull(domain.getId());
                })
                .assertNext(domain -> {
                    assertEquals(domain2.getDomain(), domain.getDomain());
                    assertThat(domain2.getIsActive()).isTrue();
                    assertThat(domain2.getIsPrivate()).isTrue();
                    assertNotNull(domain.getId());
                })
                .expectComplete()
                .verify();
    }

    @Test
    void shouldDeleteDomain() {
        domainService.deleteDomain(domain1.getId()).block();
        var domains = domainService.getDomainsExcludingLocalhost(100);

        StepVerifier
                .create(domains)
                .expectSubscription()
                .assertNext(domain -> {
                    assertEquals(domain2.getDomain(), domain.getDomain());
                    assertThat(domain2.getIsActive()).isTrue();
                    assertThat(domain2.getIsPrivate()).isTrue();
                    assertNotNull(domain.getId());
                })
                .expectComplete()
                .verify();
    }

    @Test
    void shouldCreateDomain() {
        domainService.createDomain(domainItem).block();
        var domains = domainService.getDomainsExcludingLocalhost(100);

        StepVerifier
                .create(domains)
                .expectSubscription()
                .expectNextCount(3)
                .expectComplete()
                .verify();
    }

    @Test
    void shouldGetDomain() {
        var domainItem = domainService.getDomain(domain1.getId());

        StepVerifier
                .create(domainItem)
                .expectSubscription()
                .assertNext(domain -> {
                    assertEquals(domain1.getDomain(), domain.getDomain());
                    assertThat(domain1.getIsActive()).isTrue();
                    assertThat(domain1.getIsPrivate()).isFalse();
                    assertNotNull(domain.getId());
                })
                .expectComplete()
                .verify();
    }

    @Test
    void shouldUpdateDomain() {
        domain1.setIsActive(false);
        var domainItem = domainService.updateDomain(domain1);

        StepVerifier
                .create(domainItem)
                .expectSubscription()
                .assertNext(domain -> {
                    assertEquals(domain1.getDomain(), domain.getDomain());
                    assertThat(domain1.getIsActive()).isFalse();
                    assertThat(domain1.getIsPrivate()).isFalse();
                    assertNotNull(domain.getId());
                })
                .expectComplete()
                .verify();
    }



}