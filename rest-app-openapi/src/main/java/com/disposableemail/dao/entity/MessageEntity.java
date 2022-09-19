package com.disposableemail.dao.entity;

import com.disposableemail.rest.model.Address;
import com.disposableemail.rest.model.Attachment;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
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
    private String msgid;
    private List<Address> from;
    private List<Address> to;
    private List<Address> cc;
    private List<Address> bcc;
    private String subject;
    private Boolean seen;
    private Boolean flagged;
    private Boolean isDeleted;
    private String text;
    private List<String> html;
    private Boolean hasAttachments;
    private List<Attachment> attachments;
    private Integer size;
    private String downloadUrl;

    @CreatedDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private java.time.LocalDateTime createdAt;

    @LastModifiedBy
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private java.time.LocalDateTime updatedAt;
}