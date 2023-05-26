package com.disposableemail.apache.james.mailet.collector.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.time.Instant;

@Data
@NoArgsConstructor
public class Account {
    private ObjectId id;
    private String address;
    private String mailboxId;
    private Boolean isDisabled;
    private Boolean isDeleted;
    private Integer quota;
    private Integer used;
    private Instant createdAt;
    private Instant updatedAt;

}
