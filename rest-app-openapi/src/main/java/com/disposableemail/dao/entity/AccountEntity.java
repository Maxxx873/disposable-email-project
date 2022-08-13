package com.disposableemail.dao.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Document
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class AccountEntity {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @Email
    @Indexed(unique = true)
    private String address;

    private Integer quota;
    private Integer used;
    private Boolean isDisabled;
    private Boolean isDeleted;

    @CreatedDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedBy
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime updatedAt = LocalDateTime.now();

    private List<MessageEntity> messages;

}
