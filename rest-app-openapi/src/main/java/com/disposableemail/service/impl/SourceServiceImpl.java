package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.AttachmentEntity;
import com.disposableemail.dao.entity.SourceEntity;
import com.disposableemail.dao.repository.SourceRepository;
import com.disposableemail.service.api.SourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceServiceImpl implements SourceService {

    private final SourceRepository sourceRepository;

    @Override
    public Mono<SourceEntity> getSourceByMsgId(String msgid) {
        log.info("Getting a Source | msgid: {}", msgid);

        return sourceRepository.findByMsgid(msgid);
    }

    @Override
    public Mono<byte[]> downloadSource(String msgid) {
        log.info("Getting a Source as ByteArrayInputStream | msgid: {}", msgid);

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
    public Mono<String> getAttachmentName(String attachmentId) {
        log.info("Getting Attachment name | attachmentId: {}", attachmentId);

        return sourceRepository.findByAttachmentId(attachmentId).map(sourceEntity ->
                        sourceEntity.getAttachments().stream().filter(attachmentEntity ->
                                Objects.equals(attachmentEntity.getId(), attachmentId)).findFirst().orElseThrow())
                .map(AttachmentEntity::getFilename)
                .doOnSuccess(filename -> log.info("Received attachment | filename: {}", filename))
                .doOnError(e -> log.error("Attachment name not found | attachmentId: {}", attachmentId));
    }

    @Override
    public Mono<byte[]> downloadAttachment(String msgid, String attachmentId) {
        log.info("Getting an Attachment as ByteArrayInputStream | msgid: {}, attachmentId: {}", msgid, attachmentId);

        return sourceRepository.findByMsgid(msgid).map(sourceEntity ->
        {
            try {
                var attachment = sourceEntity.getAttachments().stream().filter(attachmentEntity ->
                        Objects.equals(attachmentEntity.getId(), attachmentId)).findFirst();
                var mimeMessage = new MimeMessage(null, new ByteArrayInputStream(sourceEntity.getData().getBytes()));
                var content = mimeMessage.getContent();
                if (content instanceof Multipart multiPart) {
                    var part = (MimeBodyPart) multiPart.getBodyPart(attachment.orElseThrow().getPartId());
                    return IOUtils.toByteArray(new ByteArrayInputStream(part.getInputStream().readAllBytes()));
                }
                return IOUtils.toByteArray(new ByteArrayInputStream(sourceEntity.getData().getBytes()));
            } catch (IOException | MessagingException e) {
                throw new RuntimeException(e);
            }
        });


    }
}
