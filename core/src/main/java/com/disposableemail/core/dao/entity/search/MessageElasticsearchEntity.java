package com.disposableemail.core.dao.entity.search;

import com.disposableemail.core.model.Address;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;

@Data
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Document(indexName = "mailbox_v1")
public class MessageElasticsearchEntity {

    @Id
    private String id;

    @Field(type = FieldType.Auto)
    private String routing;

    private String accountId;
    private String messageId;
    private String mailboxId;
    private String mimeMessageID;
    private List<Address> from;
    private List<Address> to;
    private List<Address> cc;
    private List<Address> bcc;
    private String subject;
    private Boolean isUnread;
    private Boolean isFlagged;
    private Boolean isDeleted;
    private String textBody;
    private List<String> htmlBody;
    private Boolean hasAttachment;
    private List<AttachmentElasticsearchEntity> attachments;
    private Integer size;
    private String downloadUrl;

    @Field(type = FieldType.Date, format = DateFormat.date, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Instant date;
}
