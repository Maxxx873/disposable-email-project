package com.disposableemail.dao.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class DomainEntity {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @Indexed(unique = true)
    private String domain;

    private Boolean isActive;
    private Boolean isPrivate;

    @CreatedDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private java.time.LocalDateTime createdAt;

    @LastModifiedBy
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private java.time.LocalDateTime updatedAt;
}
