package com.disposableemail.telegram.bot.handler.impl;

import com.disposableemail.telegram.bot.service.BotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Slf4j
@Component
@RequiredArgsConstructor
public class BotMessageHandler {

    private final BotService botService;

    public Publisher<SendMessage> processMessage(Update update) {
        return switch (update.getMessage().getText()) {
            case "/start" -> botService.registerCustomer(update);
            case "/new" -> botService.showDomains(update);
            case "/list" -> botService.showAccounts(update);
            case "/help" -> botService.showHelp(update);
            default -> botService.showAnswer(update);
        };
    }
}

