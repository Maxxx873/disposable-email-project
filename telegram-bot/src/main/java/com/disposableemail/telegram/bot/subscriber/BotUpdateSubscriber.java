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

import java.util.List;
import java.util.Objects;

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
        if (Objects.requireNonNull(t) instanceof List<?> messages) {
            messages.forEach(this::executeMessage);
        } else {
            executeMessage(t);
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

    private void executeMessage(Object message) {
        try {
            if (message instanceof SendMessage sendMessage) {
                telegramBot.execute(sendMessage);
            } else if (message instanceof SendDocument sendDocument) {
                telegramBot.execute(sendDocument);
            } else if (message instanceof EditMessageText editMessageText) {
                telegramBot.execute(editMessageText);
            } else {
                throw new IllegalStateException("Unexpected value: " + message);
            }
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
