package com.disposableemail.telegram.bot.service;

import com.disposableemail.telegram.bot.model.CallbackData;

import java.util.Optional;

public interface BotCallbackService {
    void saveCallbackData(CallbackData<?> callbackData);
    Optional<CallbackData<?>> getCallbackData(String id);
    void deleteCallbackData(String id);
}
