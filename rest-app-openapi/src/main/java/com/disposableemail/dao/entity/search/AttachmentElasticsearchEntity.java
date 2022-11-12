package com.disposableemail.dao.entity.search;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * An Attachment Elasticsearch entity.
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Document(indexName = "mailbox_v1")
public class AttachmentElasticsearchEntity {

    private String subtype;
    private String fileName;
    private String contentDisposition;
}