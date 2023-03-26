package com.disposableemail.telegram.bot.event;

import com.disposableemail.telegram.client.disposableemail.webclient.model.Credentials;

import jakarta.annotation.Nullable;

public class AccountCreationEvent extends BotEvent<Credentials> {
    public AccountCreationEvent(@Nullable Credentials credentials) {
        super(credentials);
    }
}
