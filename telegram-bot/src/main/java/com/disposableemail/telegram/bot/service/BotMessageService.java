package com.disposableemail.telegram.bot.service;

import com.disposableemail.telegram.bot.model.dto.MessageDto;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import org.reactivestreams.Publisher;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface BotMessageService {

    Publisher<SendMessage> getHtmlPart(long chatId, MessageDto messageDto);

    Publisher<?> showMessages(long chatId, AccountEntity account);
}
