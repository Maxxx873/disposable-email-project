package com.disposableemail.core.dao.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An Attachment entity.
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Document(collection = "source")
public class AttachmentEntity {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private String filename;
    private String contentType;
    private String disposition;
    private String transferEncoding;
    private Integer size;
    private Integer partId;
    private String downloadUrl;
}
