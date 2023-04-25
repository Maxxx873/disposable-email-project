package com.disposableemail.telegram.bot.service;

import com.disposableemail.telegram.client.disposableemail.webclient.model.Domain;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import org.reactivestreams.Publisher;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface BotAccountService {

    Publisher<SendMessage> createAccount(long chatId, Domain domain);

    Publisher<SendMessage> enterLogin(long chatId, String login);

    Publisher<SendMessage> enterPassword(long chatId, String password);

    Publisher<EditMessageText> deleteAccountQuestion(Message message, AccountEntity account);

    Publisher<EditMessageText> showAccount(Message message, AccountEntity account);

    Publisher<EditMessageText> deleteAccount(Message message, AccountEntity account);
}
