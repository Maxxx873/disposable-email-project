package com.disposableemail.telegram.bot.service;

import org.reactivestreams.Publisher;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface BotCustomerService {

    Publisher<SendMessage> registerCustomer(long chatId, String customerName);
    Publisher<SendMessage> showAccounts(long chatId);
    Publisher<SendMessage> showAnswer(long chatId, String message);
    Publisher<SendMessage> showHelp(long chatId);

}
