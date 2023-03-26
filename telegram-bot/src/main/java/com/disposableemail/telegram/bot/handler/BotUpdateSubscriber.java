package com.disposableemail.telegram.bot.handler;

import com.disposableemail.telegram.bot.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@RequiredArgsConstructor
public class BotUpdateSubscriber implements Subscriber<SendMessage> {

    private final TelegramBot telegramBot;

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(SendMessage sendMessage) {
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error(throwable.getLocalizedMessage());
    }

    @Override
    public void onComplete() {
        log.info("BotUpdateSubscriber is done!");
    }
}
