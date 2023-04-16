package com.disposableemail.telegram.bot.handler;

import org.reactivestreams.Publisher;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotHandler {
    Publisher<?> handleUpdate(Update update);
}
