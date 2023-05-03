package com.disposableemail.core.dao.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * An Attachment entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "source")
public class AttachmentEntity implements Serializable {

    @Id
    private String id;

    private String filename;
    private String contentType;
    private String disposition;
    private String transferEncoding;
    private Integer size;
    private Integer partId;
    private String downloadUrl;
}
