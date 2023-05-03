package com.disposableemail.core.dao.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * A Source entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "source")
public class SourceEntity extends Auditable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String msgid;

    private String data;
    private List<AttachmentEntity> attachments;

}
