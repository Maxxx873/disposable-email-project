package com.disposableemail.telegram.starter;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

@Data
@RequiredArgsConstructor
public class BotConfig {
    private final String botName;
    private final String token;
    private final List<BotCommand> listOfCommands;
}
