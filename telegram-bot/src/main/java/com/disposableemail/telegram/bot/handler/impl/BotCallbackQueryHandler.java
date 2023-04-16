package com.disposableemail.telegram.bot.handler.impl;

import com.disposableemail.telegram.bot.model.CallbackData;
import com.disposableemail.telegram.bot.replier.BotReplier;
import com.disposableemail.telegram.bot.service.BotAccountService;
import com.disposableemail.telegram.bot.service.BotCallbackService;
import com.disposableemail.telegram.bot.service.BotMessageService;
import com.disposableemail.telegram.client.disposableemail.webclient.model.Domain;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import com.disposableemail.telegram.service.CustomerService;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.disposableemail.telegram.bot.replier.BotReplier.ADDITIONAL_HELP;
import static com.disposableemail.telegram.bot.replier.BotReplier.OUTDATED;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.editMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotCallbackQueryHandler {

    private final BotCallbackService botCallbackService;
    private final CustomerService customerService;
    private final BotAccountService botAccountService;
    private final BotReplier botReplier;
    private final BotMessageService botMessageService;

    private Optional<CallbackData<?>> callbackData;

    @Transactional
    public Publisher<?> processCallbackQuery(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        var customer = customerService.getByChatId(chatId);
        callbackData = botCallbackService.getCallbackData(callbackQuery.getData());

        if (callbackData.isEmpty()) {
            String messageText = botReplier.reply(OUTDATED) + botReplier.reply(ADDITIONAL_HELP);
            return Mono.just(editMessage(callbackQuery.getMessage(), EmojiParser.parseToUnicode(messageText)));
        }

        if (customer.isPresent()) {
            return switch (callbackData.get().getData()) {
                case AccountEntity account -> choiceAccountAnswer(chatId, account);
                case Domain domain -> botAccountService.createAccount(chatId, domain);
                default -> throw new IllegalStateException("Unexpected value: " + callbackData.get().getData());
            };
        }
        return Mono.empty();
    }

    private Publisher<?> choiceAccountAnswer(long chatId, AccountEntity account) {
        if (callbackData.isPresent()) {
            return switch (callbackData.get().getAction()) {
                case GET -> botMessageService.showMessages(chatId, account);
                case DELETE -> {
                    botCallbackService.deleteCallbackData(callbackData.get().getId());
                    yield botAccountService.deleteAccount(chatId, account);
                }
            };
        }
        return Mono.empty();
    }

}
