package com.disposableemail.telegram.bot.handler;

import org.reactivestreams.Publisher;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotHandler {
    Publisher<SendMessage> handleUpdate(Update update);
}
