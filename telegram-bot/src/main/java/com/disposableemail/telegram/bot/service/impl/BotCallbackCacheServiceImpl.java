package com.disposableemail.telegram.bot.service.impl;

import com.disposableemail.telegram.bot.model.CallbackData;
import com.disposableemail.telegram.bot.service.BotCallbackService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotCallbackCacheServiceImpl implements BotCallbackService {

    private final Cache<String, CallbackData<?>> cache = Caffeine.newBuilder()
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    @Override
    public void saveCallbackData(CallbackData<?> callbackData) {
        callbackData.setId(UUID.randomUUID().toString());
        log.info("Writing CallbackData to the Caffeine cache : {}", callbackData);
        cache.put(callbackData.getId(), callbackData);
    }

    @Override
    public Optional<CallbackData<?>> getCallbackData(String id) {
        log.info("Reading CallbackData from the Caffeine cache : {}", id);
        return Optional.ofNullable(cache.getIfPresent(id));
    }

    @Override
    public void deleteCallbackData(String id) {
        log.info("Deleting CallbackData from the Caffeine cache : {}", id);
        cache.invalidate(id);
    }
}
