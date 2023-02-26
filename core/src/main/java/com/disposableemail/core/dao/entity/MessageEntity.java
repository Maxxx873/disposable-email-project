package com.disposableemail.core.dao.entity;

import com.disposableemail.core.model.Address;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

/**
 * A Message entity.
 */
@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Document(collection = "message")
public class MessageEntity {

    @Id
    @Setter(AccessLevel.NONE)
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
    private List<String> html;
    private Boolean hasAttachment;
    private List<AttachmentEntity> attachments;
    private Integer size;

    @CreatedDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private java.time.LocalDateTime createdAt;

    @LastModifiedBy
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private java.time.LocalDateTime updatedAt;
}