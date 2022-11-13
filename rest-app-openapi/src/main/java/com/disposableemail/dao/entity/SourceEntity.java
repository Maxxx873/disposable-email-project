package com.disposableemail.dao.entity;

import com.disposableemail.rest.model.Attachment;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * A Source entity.
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Document(collection = "source")
public class SourceEntity {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @Indexed(unique = true)
    private String msgid;

    private String data;
    private List<Attachment> attachments;

}
