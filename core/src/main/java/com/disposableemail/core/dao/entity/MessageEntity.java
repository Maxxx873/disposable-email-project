package com.disposableemail.core.dao.entity;

import com.disposableemail.core.model.Address;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.List;

/**
 * A Message entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "message")
public class MessageEntity extends Auditable {

    @Id
    private String id;

    private String accountId;

    @Indexed(unique = true)
    private String msgid;

    private List<Address> from;
    private List<Address> to;
    private List<Address> cc;
    private List<Address> bcc;
    private String subject;
    private Boolean isUnread;
    private Boolean isFlagged;
    private Boolean isDeleted;
    private String text;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant sentDate;

    private List<String> html;
    private Boolean hasAttachment;
    private List<AttachmentEntity> attachments;
    private Integer size;
}