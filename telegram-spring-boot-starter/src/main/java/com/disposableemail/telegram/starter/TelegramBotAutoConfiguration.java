package com.disposableemail.telegram.starter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.generics.TelegramBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnBean(TelegramBot.class)
@ConditionalOnProperty(prefix = "telegram.bot.commands", name = "start")
public class TelegramBotAutoConfiguration {

    @Value("${telegram.bot.name}")
    private String botName;

    @Value("${telegram.bot.token}")
    private String token;

    @Bean
    @ConfigurationProperties(prefix = "telegram.bot.commands")
    public Map<String, String> commandsMappings() {
        return new HashMap<>();
    }

    @Bean
    public BotConfig botConfig() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        this.commandsMappings().forEach((k, v) -> listOfCommands.add(new BotCommand("/" + k, v)));
        return new BotConfig(botName, token, listOfCommands);
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        var source = new ResourceBundleMessageSource();
        source.setBasenames("messages/replies");
        source.setDefaultEncoding("UTF-8");
        return source;
    }

}

