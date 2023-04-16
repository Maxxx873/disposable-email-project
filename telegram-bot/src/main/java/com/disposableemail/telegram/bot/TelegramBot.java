package com.disposableemail.telegram.bot;

import com.disposableemail.telegram.bot.config.BotConfig;
import com.disposableemail.telegram.bot.handler.BotHandler;
import com.disposableemail.telegram.bot.subscriber.BotUpdateSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotHandler botHandler;
    private final BotConfig config;

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        botHandler.handleUpdate(update).subscribe(new BotUpdateSubscriber<>(this));
    }

}
