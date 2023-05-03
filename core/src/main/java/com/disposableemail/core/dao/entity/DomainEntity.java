package com.disposableemail.core.dao.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A Domain entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "domain")
public class DomainEntity extends Auditable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String domain;

    private Boolean isActive;
    private Boolean isPrivate;

}
