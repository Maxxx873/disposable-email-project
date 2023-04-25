package com.disposableemail.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

import static java.time.ZonedDateTime.now;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Event<T> {

    public enum Type {
        GETTING_MESSAGES,
        START_CREATING_ACCOUNT,
        KEYCLOAK_REGISTER_CONFIRMATION,
        ACCOUNT_CREATED_IN_MAIL_SERVICE,
        MAILBOX_CREATED_IN_MAIL_SERVICE,
        QUOTA_SIZE_UPDATED_IN_MAIL_SERVICE,
        START_DELETING_ACCOUNT,
        KEYCLOAK_DELETING_CONFIRMATION,
        //TODO delete
    }

    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private final Type type;
    private final T data;

    @Builder.Default
    private ZonedDateTime eventCreatedAt = now();
}
