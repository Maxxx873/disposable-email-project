package com.disposableemail.dao.entity.search;

import com.disposableemail.rest.model.Address;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Document(indexName = "mailbox_v1")
public class MessageElasticsearchEntity {

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
    private OffsetDateTime date;
}
