package com.disposableemail.telegram.bot.event;

import lombok.AllArgsConstructor;

import javax.annotation.Nullable;

@AllArgsConstructor
public class BotEvent<T> {
    private final T object;

    @Nullable
    public T get() {
        return object;
    }
}
