package com.disposableemail.core.service.impl.search;

import com.disposableemail.config.TestConfig;
import com.disposableemail.core.dao.entity.search.MessageElasticsearchEntity;
import com.disposableemail.core.dao.repository.search.MessageElasticsearchRepository;
import com.disposableemail.core.service.api.search.MessageElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Durations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"search.service=elasticsearch"})
class MessageElasticsearchServiceIntegrationTest {

    @Container
    static ElasticsearchContainer esContainer = new ElasticsearchContainer(DockerImageName
            .parse("blacktop/elasticsearch:7.17.2")
            .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"))
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false");

    @DynamicPropertySource
    static void esProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", esContainer::getHttpHostAddress);
    }

    @Autowired
    MessageElasticsearchRepository messageElasticsearchRepository;

    @Autowired
    MessageElasticsearchService messageElasticsearchService;

    private static final MessageElasticsearchEntity messageEntity = MessageElasticsearchEntity.builder()
            .messageId("f6c394a0-a1a2-11ed-9c7c-45671b00b811")
            .mailboxId("dfe6fe90-9732-11ed-8985-859682c50720")
            .isDeleted(false)
            .build();

    @BeforeEach
    public void setUp() throws IOException {

        messageElasticsearchRepository.save(messageEntity)
                .subscribe();

        await().pollDelay(Durations.TWO_SECONDS).until(() -> true);

    }

    @Test
    void testUpdateMessageById() {

        messageElasticsearchService.softDeleteMessageById(messageEntity.getMessageId()).subscribe();

        await().pollDelay(Durations.TWO_SECONDS).until(() -> true);

        StepVerifier.create(messageElasticsearchRepository.findAll())
                .expectSubscription()
                .assertNext(message -> {
                    assertThat(message.getMessageId()).isEqualTo(messageEntity.getMessageId());
                    assertThat(message.getIsDeleted()).isTrue();
                })
                .expectComplete()
                .verify();
    }

    @Test
    void testDeleteById() {

        messageElasticsearchService.deleteMessageById(messageEntity.getMessageId());

        await().pollDelay(Durations.TWO_SECONDS).until(() -> true);

        StepVerifier.create(messageElasticsearchRepository.findAll())
                .expectSubscription()
                .expectComplete()
                .verify();
    }

}