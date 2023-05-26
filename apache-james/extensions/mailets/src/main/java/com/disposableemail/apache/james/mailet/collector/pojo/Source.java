package com.disposableemail.apache.james.mailet.collector.pojo;


import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class Source {
    private ObjectId id;
    private String msgid;
    private String data;
    private List<Attachment> attachments;
    private Instant createdAt;
}
