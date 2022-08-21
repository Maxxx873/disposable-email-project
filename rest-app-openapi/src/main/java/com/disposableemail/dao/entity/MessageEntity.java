package com.disposableemail.dao.entity;

import com.disposableemail.rest.model.Address;
import com.disposableemail.rest.model.Attachment;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Document(indexName = "mailbox_v1")
public class MessageEntity {

    private String messageId;
    private String accountId;
    private String msgid;
    private List<Address> from;
    private List<Address> to;
    private List<Address> cc;
    private List<Address> bcc;
    private String subject;
    private Boolean isUnread;
    private Boolean isFlagged;
    private Boolean isDeleted;
    private List<String> verifications;
    private Boolean retention;

    private LocalDateTime retentionDate;

    private String textBody;
    private List<String> htmlBody;
    private Boolean hasAttachment;
    private List<Attachment> attachments;
    private Integer size;
    private String downloadUrl;

    @Field(type = FieldType.Date, format = DateFormat.date, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private OffsetDateTime date;

    private LocalDateTime updatedAt;
}
