package com.disposableemail.core.event;

import com.disposableemail.core.model.Credentials;
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
        MAIL_DELETING_ACCOUNT,
        DELETING_ACCOUNT,
        DOMAIN_CREATED,
        DOMAIN_DELETED,
    }

    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private final Type type;
    private final T data;

    @Builder.Default
    private Instant eventCreatedAt = Instant.now();

    public String getLogString() {
        if (this.getData() instanceof Credentials) {
            return String.join(", ",
                    "Event(id=" + this.getId(),
                    "type=" + this.getType(),
                    "data=class Credentials {address=" + ((Credentials) this.getData()).getAddress() + "}",
                    "eventCreatedAt=" + this.getEventCreatedAt() + ")");
        }
        return this.toString();
    }
}
