package com.disposableemail.telegram.bot.service;

import com.disposableemail.telegram.dao.entity.AccountEntity;
import org.reactivestreams.Publisher;

public interface BotMessageService {

    Publisher<?> showMessages(long chatId, AccountEntity account);
}
