package com.disposableemail.telegram.bot.event;

import com.disposableemail.telegram.client.disposableemail.webclient.model.Credentials;
import jakarta.annotation.Nullable;

public class AccountDeletionEvent extends BotEvent<Credentials> {
    public AccountDeletionEvent(@Nullable Credentials credentials) {
        super(credentials);
    }
}
