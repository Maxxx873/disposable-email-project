package com.disposableemail.telegram.bot.handler.impl;

import com.disposableemail.telegram.bot.handler.BotHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotHandlerImpl implements BotHandler {

    private final BotCallbackQueryHandler callbackQueryHandler;
    private final BotMessageHandler messageHandler;

    @Override
    public Publisher<?> handleUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            return callbackQueryHandler.processCallbackQuery(update.getCallbackQuery());
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            return messageHandler.processMessage(update);
        }
        return Mono.empty();
    }
}