package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.SourceEntity;
import com.disposableemail.dao.repository.SourceRepository;
import com.disposableemail.service.api.SourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceServiceImpl implements SourceService {

    private final SourceRepository sourceRepository;

    @Override
    public Mono<SourceEntity> getSourceByMsgId(String msgid) {
        log.info("Getting a Source by msgid {}", msgid);

        return sourceRepository.findByMsgid(msgid);
    }

    @Override
    public Mono<byte[]> downloadSource(String msgid) {
        log.info("Getting a Source as ByteArrayInputStream by msgid {}", msgid);

        return sourceRepository.findByMsgid(msgid).map(sourceEntity ->
        {
            try {
                return IOUtils.toByteArray(new ByteArrayInputStream(sourceEntity.getData().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Mono<byte[]> downloadAttachment(String msgid, String attachmentId) {
        log.info("Getting an Attachment as ByteArrayInputStream by msgid {}, attachmentId {}", msgid, attachmentId);

        return null;
    }
}
