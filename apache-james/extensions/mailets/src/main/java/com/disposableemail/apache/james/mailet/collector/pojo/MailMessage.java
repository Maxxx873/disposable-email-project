package com.disposableemail.apache.james.mailet.collector.pojo;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class MailMessage {

    private ObjectId id;
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
    private String text;
    private List<String> html;
    private boolean hasAttachment;
    private List<Attachment> attachments;
    private Integer size;
    private Instant sentDate;
    private Instant createdAt;
    private Instant updatedAt;

}
