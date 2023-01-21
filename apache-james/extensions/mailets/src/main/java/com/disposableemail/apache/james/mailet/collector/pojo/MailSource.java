package com.disposableemail.apache.james.mailet.collector.pojo;


import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MailSource {
    private ObjectId id;
    private String msgid;
    private String data;
    private List<MailAttachment> attachments;
    private LocalDateTime createdAt;
}
