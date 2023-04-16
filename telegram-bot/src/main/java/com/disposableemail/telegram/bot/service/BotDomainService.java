package com.disposableemail.telegram.bot.service;

import org.reactivestreams.Publisher;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface BotDomainService {
    Publisher<SendMessage> showDomains(long chatId);
}
