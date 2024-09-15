package com.disposableemail.telegram.bot.config;

import com.disposableemail.telegram.bot.TelegramBot;
import com.disposableemail.telegram.starter.BotConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotInitializer {

    private final TelegramBot bot;
    private final BotConfig config;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        try {
            var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            bot.execute(new SetMyCommands(config.getListOfCommands(), new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e) {
            log.error("Error occurred: {}", e.getMessage());
        }
    }
}
