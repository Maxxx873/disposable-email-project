package com.disposableemail.telegram.bot.service.impl;

import com.disposableemail.telegram.bot.model.CallbackData;
import com.disposableemail.telegram.bot.replier.BotReplier;
import com.disposableemail.telegram.bot.service.BotCallbackService;
import com.disposableemail.telegram.bot.service.BotCustomerService;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import com.disposableemail.telegram.service.CustomerService;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static com.disposableemail.telegram.bot.handler.BotState.START;
import static com.disposableemail.telegram.bot.model.BotAction.DELETE;
import static com.disposableemail.telegram.bot.model.BotAction.GET;
import static com.disposableemail.telegram.bot.replier.BotReplier.*;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareSendMessage;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareSendMessageWithInlineKeyboard;
import static com.disposableemail.telegram.dao.entity.CustomerEntity.getNewCustomer;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BotCustomerServiceImpl implements BotCustomerService {

    private final BotCallbackService botCallbackService;
    private final CustomerService customerService;
    private final BotReplier botReplier;

    @Override
    public Mono<SendMessage> registerCustomer(long chatId, String customerName) {
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            customer.get().setBotState(START);
            customerService.save(customer.get());
        } else {
            customerService.save(getNewCustomer(chatId, customerName));
        }
        String messageText = botReplier.reply(REGISTERED) + botReplier.reply(ADDITIONAL_HELP);
        return Mono.just(prepareSendMessage(chatId, EmojiParser.parseToUnicode(messageText + " " + customerName)));
    }

    @Override
    public Publisher<SendMessage> showHelp(long chatId) {
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            customer.get().setBotState(START);
            customerService.save(customer.get());
        }
        return getDefaultMessage(HELP, chatId);
    }

    @Override
    public Publisher<SendMessage> showAccounts(long chatId) {
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            String reply = botReplier.reply(ACCOUNTS_NOT_FOUND);
            String answer = EmojiParser.parseToUnicode(reply);
            return Flux.fromStream(customer.get().getAccounts().stream()
                            .map(accountEntity -> {
                                var callbackData = Arrays.asList(
                                        CallbackData.<AccountEntity>builder()
                                                .text(botReplier.reply(BUTTON_ACCOUNT_MESSAGES))
                                                .data(accountEntity)
                                                .action(GET)
                                                .build(),
                                        CallbackData.<AccountEntity>builder()
                                                .text(botReplier.reply(BUTTON_ACCOUNT_DELETE))
                                                .data(accountEntity)
                                                .action(DELETE)
                                                .build());
                                callbackData.forEach(botCallbackService::saveCallbackData);
                                return prepareSendMessageWithInlineKeyboard(customer.get().getChatId(), callbackData, accountEntity.getAddress());
                            }))
                    .defaultIfEmpty(prepareSendMessage(chatId, answer));
        } else {
            return getDefaultMessage(NOT_REGISTERED, chatId);
        }
    }

    @Override
    public Publisher<SendMessage> showAnswer(long chatId, String message) {
        return Mono.just(prepareSendMessage(chatId, EmojiParser.parseToUnicode(botReplier.reply(UNKNOWN))));
    }

    private Mono<SendMessage> getDefaultMessage(String messageKey, long chatId) {
        String messageText = botReplier.reply(messageKey) + botReplier.reply(ADDITIONAL_HELP);
        return Mono.just(prepareSendMessage(chatId, EmojiParser.parseToUnicode(messageText)));
    }

}
