package com.disposableemail.core.service.api;

import com.disposableemail.core.dao.entity.AttachmentEntity;
import com.disposableemail.core.dao.entity.SourceEntity;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Provides methods for managing source documents in the system.
 */
public interface SourceService {

    /**
     * Retrieves a source document by its message ID.
     *
     * @param msgid the message ID of the source document
     * @return a Mono that emits the source document entity
     */
    Mono<SourceEntity> getSourceByMsgId(String msgid);

    /**
     * Downloads the source document with the specified ID.
     *
     * @param id the ID of the source document
     * @return a Mono that emits the source document content as a byte array
     */
    Mono<byte[]> downloadSource(String id);

    /**
     * Downloads the attachment with the specified ID for the source document with the specified message ID.
     *
     * @param id the message ID of the source document
     * @param attachmentId the ID of the attachment
     * @return a Mono that emits the attachment content as a byte array
     */
    Mono<byte[]> downloadAttachment(String id, String attachmentId);

    /**
     * Retrieves the name of the attachment with the specified ID.
     *
     * @param attachmentId the ID of the attachment
     * @return a Mono that emits the attachment name as a string
     */
    Mono<String> getAttachmentName(String attachmentId);

    /**
     * Retrieves the list of attachments for the source document with the specified message ID.
     *
     * @param msgid the message ID of the source document
     * @return a Mono that emits a list of attachment entities
     */
    Mono<List<AttachmentEntity>> getAttachments(String msgid);
}

