package com.disposableemail.rest.delegate;

import com.disposableemail.AbstractSpringControllerIntegrationTest;
import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.model.Domain;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class DomainApiDelegateImplTest extends AbstractSpringControllerIntegrationTest {

    @Test
    void shouldGetDomainCollectionWithQueryParams() {
        int domainCollectionSize = 2;
        when(domainService.getDomainsExcludingLocalhost(domainCollectionSize)).thenReturn(Flux.fromIterable(testDomainEntities));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/domains")
                        .queryParam("size", domainCollectionSize)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(DomainEntity.class)
                .hasSize(domainCollectionSize);

        verify(domainService, times(1)).getDomainsExcludingLocalhost(domainCollectionSize);
    }

    @Test
    void shouldGetDomainCollectionWithDefaultSize() {
        int defaultCollectionSize = 1;
        when(domainService.getDomainsExcludingLocalhost(defaultCollectionSize)).thenReturn(Flux.just(testDomainEntities.get(0)));

        webTestClient.get()
                .uri("/api/v1/domains")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Domain.class)
                .hasSize(defaultCollectionSize);

        verify(domainService, times(1)).getDomainsExcludingLocalhost(defaultCollectionSize);
    }

    @Test
    void shouldReturnDomainItem() {
        String id = testDomainEntities.get(0).getId();
        when(domainService.getDomain(id)).thenReturn(Mono.just(testDomainEntities.get(0)));

        webTestClient.get()
                .uri("/api/v1/domains/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Domain.class)
                .isEqualTo(domainMapper.domainEntityToDomain(testDomainEntities.get(0)));

        verify(domainService, times(1)).getDomain(id);
    }

    @Test
    void shouldThrowExceptionWhenDomainNotFound() {
        String id = testDomainEntities.get(0).getId();
        when(domainService.getDomain(id)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/domains/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(domainService, times(1)).getDomain(id);
    }
}