package com.disposableemail.core.service.impl;

import com.disposableemail.AbstractSpringIntegrationTest;
import com.disposableemail.core.dao.entity.AttachmentEntity;
import com.disposableemail.core.dao.entity.SourceEntity;
import com.disposableemail.core.exception.custom.AttachmentNotFoundException;
import com.disposableemail.core.exception.custom.MessageNotFoundException;
import com.disposableemail.core.exception.custom.SourceNotFoundException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SourceServiceImplTest extends AbstractSpringIntegrationTest {

    private SourceEntity testSource;

    @BeforeEach
    public void setUp() throws IOException {
        var path = Paths.get("src/test/resources/test.eml");
        var data = Files.readString(path, StandardCharsets.UTF_8);
        var attachment1 = AttachmentEntity.builder()
                .id("0")
                .contentType("message/delivery-status")
                .disposition("attachment")
                .filename("first.dat")
                .downloadUrl("test1")
                .partId(1)
                .size(339)
                .transferEncoding("7bit")
                .build();
        var attachment2 = AttachmentEntity.builder()
                .id("1")
                .contentType("message/rfc822")
                .disposition("Attachment")
                .filename("second.dat")
                .downloadUrl("test2")
                .partId(2)
                .size(618)
                .build();

        testSource = SourceEntity.builder()
                .id("1")
                .msgid("<1976018613.1.1685302342282@james.local>")
                .data(data)
                .attachments(Arrays.asList(attachment1, attachment2))
                .build();

        sourceRepository.save(testSource).block();
    }

    @AfterAll
    public void cleanUp() {
        sourceRepository.deleteAll().block();
    }


    @Test
    void shouldRetrieveSource() {
        var result = sourceService.getSourceByMsgId(testSource.getMsgid()).block();

        assertNotNull(result);

        assertThat(testSource)
                .usingRecursiveComparison()
                .ignoringFields("updatedAt")
                .isEqualTo(result);
    }

    @Test
    void shouldThrowExceptionWhenSourceIsEmpty() {
        var result = sourceService.getSourceByMsgId("nonExistentId");

        assertThrows(SourceNotFoundException.class, result::block);
    }

    @Test
    void shouldRetrieveSourceAsByteArrayInputStream() {
        Mono<byte[]> result = sourceService.downloadSource(testSource.getMsgid());

        assertNotNull(result);

        byte[] expectedBytes = testSource.getData().getBytes();
        byte[] actualBytes = result.block();

        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    void shouldThrowExceptionWhenSourceNotFound() {
        Mono<byte[]> result = sourceService.downloadSource("nonExistentId");

        assertThrows(SourceNotFoundException.class, result::block);
    }

    @Test
    void shouldRetrieveAttachmentName() {
        var result = sourceService.getAttachmentName(testSource.getAttachments().get(0).getId()).block();

        assertNotNull(result);

        assertThat(testSource.getAttachments().get(0).getFilename()).isEqualTo(result);
    }

    @Test
    void shouldThrowExceptionWhenAttachmentNameNotFound() {
        var result = sourceService.getAttachmentName("nonExistentId");

        assertThrows(AttachmentNotFoundException.class, result::block);
    }

    @Test
    void shouldRetrieveAttachments() {
        var result = sourceService.getAttachments(testSource.getMsgid()).block();

        assertNotNull(result);

        assertThat(testSource.getAttachments())
                .usingRecursiveComparison()
                .ignoringFields("updatedAt")
                .isEqualTo(result);
    }

    @Test
    void shouldDownloadAttachment() throws IOException {
        var firstPath = Paths.get("src/test/resources/first.dat");
        var secondPath = Paths.get("src/test/resources/second.dat");
        var firstAttachmentId = testSource.getAttachments().get(0).getId();
        var secondAttachmentId = testSource.getAttachments().get(1).getId();

        Mono<byte[]> firstResult = sourceService.downloadAttachment(testSource.getMsgid(), firstAttachmentId);
        Mono<byte[]> secondResult = sourceService.downloadAttachment(testSource.getMsgid(), secondAttachmentId);

        assertNotNull(firstResult);
        assertNotNull(secondResult);

        byte[] firstExpectedBytes = Files.readAllBytes(firstPath);
        byte[] secondExpectedBytes = Files.readAllBytes(secondPath);

        byte[] firstActualBytes = firstResult.block();
        byte[] secondActualBytes = secondResult.block();

        assertArrayEquals(firstExpectedBytes, firstActualBytes);
        assertArrayEquals(secondExpectedBytes, secondActualBytes);
    }

    @Test
    void shouldThrowMessageNotFoundException() {
        Mono<byte[]> result = sourceService.downloadAttachment(testSource.getMsgid(), "nonExistentId");

        assertThrows(MessageNotFoundException.class, result::block);
    }
}