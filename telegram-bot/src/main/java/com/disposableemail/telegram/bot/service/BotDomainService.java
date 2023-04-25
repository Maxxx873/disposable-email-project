package com.disposableemail.telegram.bot.service;

import org.reactivestreams.Publisher;

public interface BotDomainService {
    Publisher<?> showDomains(long chatId);
}
