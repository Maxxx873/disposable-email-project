package com.disposableemail.telegram.bot.service.impl;

import com.disposableemail.telegram.bot.error.BotErrorHandler;
import com.disposableemail.telegram.bot.model.CallBackDataFactory;
import com.disposableemail.telegram.bot.replier.BotReplier;
import com.disposableemail.telegram.bot.service.BotCallbackService;
import com.disposableemail.telegram.bot.service.BotCustomerService;
import com.disposableemail.telegram.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.disposableemail.telegram.bot.handler.BotState.START;
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
    private final CallBackDataFactory callBackDataFactory;
    private final BotErrorHandler errorHandler;

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
        return Mono.just(prepareSendMessage(chatId, messageText));
    }

    @Override
    public Publisher<SendMessage> showHelp(long chatId) {
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            customer.get().setBotState(START);
            customerService.save(customer.get());
        }
        return Mono.just(prepareSendMessage(chatId, botReplier.reply(HELP) + botReplier.reply(ADDITIONAL_HELP)));
    }

    @Override
    public Publisher<SendMessage> showAccounts(long chatId) {
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            customer.get().setBotState(START);
            customerService.save(customer.get());
            String reply = botReplier.reply(ACCOUNTS_NOT_FOUND);
            return Flux.fromStream(customer.get().getAccounts().stream()
                            .map(accountEntity -> {
                                var callbackData = callBackDataFactory.getCallBackDataForAccountsShow(accountEntity);
                                callbackData.forEach(botCallbackService::saveCallbackData);
                                return prepareSendMessageWithInlineKeyboard(customer.get().getChatId(),
                                        callbackData, accountEntity.getAddress());
                            }))
                    .defaultIfEmpty(prepareSendMessage(chatId, reply));
        } else {
            return Mono.just(errorHandler.handleNotRegisteredCustomerError(chatId));
        }
    }

    @Override
    public Publisher<SendMessage> showAnswer(long chatId, String message) {
        return Mono.just(prepareSendMessage(chatId, botReplier.reply(UNKNOWN)));
    }

}
