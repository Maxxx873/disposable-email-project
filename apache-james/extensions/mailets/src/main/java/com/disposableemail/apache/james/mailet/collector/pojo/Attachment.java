package com.disposableemail.apache.james.mailet.collector.pojo;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class Attachment {
    private ObjectId id;
    private String filename;
    private String contentType;
    private String disposition;
    private String transferEncoding;
    private int size;
    private int partId;

}
