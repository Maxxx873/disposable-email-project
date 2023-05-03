package com.disposableemail.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Event<T> {

    public enum Type {
        GETTING_MESSAGES,
        START_CREATING_ACCOUNT,
        AUTH_REGISTER_CONFIRMATION,
        ACCOUNT_CREATED_IN_MAIL_SERVICE,
        MAILBOX_CREATED_IN_MAIL_SERVICE,
        QUOTA_SIZE_UPDATED_IN_MAIL_SERVICE,
        AUTH_DELETING_ACCOUNT,
        MAIL_DELETING_ACCOUNT
    }

    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private final Type type;
    private final T data;

    @Builder.Default
    private Instant eventCreatedAt = Instant.now();
}
