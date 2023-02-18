package com.disposableemail.core.service.api;

import com.disposableemail.core.dao.entity.AttachmentEntity;
import com.disposableemail.core.dao.entity.SourceEntity;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SourceService {

    Mono<SourceEntity> getSourceByMsgId(String msgid);

    Mono<byte[]> downloadSource(String id);

    Mono<byte[]> downloadAttachment(String id, String attachmentId);

    Mono<String> getAttachmentName(String attachmentId);

    Mono<List<AttachmentEntity>> getAttachments(String msgid);

}
