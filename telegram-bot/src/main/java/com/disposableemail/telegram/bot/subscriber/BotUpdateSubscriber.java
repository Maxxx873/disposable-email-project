package com.disposableemail.telegram.bot.subscriber;

import com.disposableemail.telegram.bot.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@RequiredArgsConstructor
public class BotUpdateSubscriber<T> implements Subscriber<T> {

    private final TelegramBot telegramBot;

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
        try {
            if (t instanceof SendMessage sendMessage) {
                telegramBot.execute(sendMessage);
            }
            if (t instanceof SendDocument sendDocument) {
                telegramBot.execute(sendDocument);
            }
            if (t instanceof EditMessageText editMessageText) {
                telegramBot.execute(editMessageText);
            }
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
