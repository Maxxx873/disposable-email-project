package com.disposableemail.telegram.bot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
public class BotConfig {
    @Value("${telegram.bot.name}")
    private String botName;

    @Value("${telegram.bot.token}")
    private String token;

    private List<BotCommand> listOfCommands;

    public BotConfig() {
        this.listOfCommands = new ArrayList<>();
        this.listOfCommands.add(new BotCommand("/start", "start using disposable email"));
        this.listOfCommands.add(new BotCommand("/new", "add new email account"));
        this.listOfCommands.add(new BotCommand("/list", "my list of accounts"));
        this.listOfCommands.add(new BotCommand("/help", "info how to use disposable email bot"));
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        var source = new ResourceBundleMessageSource();
        source.setBasenames("messages/replies");
        source.setDefaultEncoding("UTF-8");
        return source;
    }
}
