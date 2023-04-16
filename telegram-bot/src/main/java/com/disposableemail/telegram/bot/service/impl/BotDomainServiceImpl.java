package com.disposableemail.telegram.bot.service.impl;

import com.disposableemail.telegram.bot.model.BotAction;
import com.disposableemail.telegram.bot.model.CallbackData;
import com.disposableemail.telegram.bot.replier.BotReplier;
import com.disposableemail.telegram.bot.service.BotCallbackService;
import com.disposableemail.telegram.bot.service.BotDomainService;
import com.disposableemail.telegram.service.CustomerService;
import com.disposableemail.telegram.service.EmailService;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.disposableemail.telegram.bot.handler.BotState.WAITING_FOR_DOMAIN_CHOICE;
import static com.disposableemail.telegram.bot.replier.BotReplier.ACCOUNTS_ADD;
import static com.disposableemail.telegram.bot.replier.BotReplier.NOT_REGISTERED;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareSendMessage;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareSendMessageWithInlineKeyboard;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BotDomainServiceImpl implements BotDomainService {

    private final BotCallbackService botCallbackService;
    private final EmailService emailService;
    private final CustomerService customerService;
    private final BotReplier botReplier;

    private static final int DEFAULT_SIZE = 10;

    @Override
    public Publisher<SendMessage> showDomains(long chatId) {
        String messageText;
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            messageText = EmojiParser.parseToUnicode(botReplier.reply(ACCOUNTS_ADD));
            customer.get().setBotState(WAITING_FOR_DOMAIN_CHOICE);
            customerService.save(customer.get());
            var callbackData = emailService.getDomains(DEFAULT_SIZE)
                    .map(domain -> CallbackData.builder()
                            .text(domain.getDomain())
                            .action(BotAction.GET)
                            .data(domain)
                            .build())
                    .collectList().block();
            Objects.requireNonNull(callbackData).forEach(botCallbackService::saveCallbackData);
            return Mono.just(prepareSendMessageWithInlineKeyboard(customer.get().getChatId(), callbackData, messageText));
        } else {
            return getDefaultMessage(chatId);
        }
    }

    private Mono<SendMessage> getDefaultMessage(long chatId) {
        String messageText = botReplier.reply(NOT_REGISTERED);
        return Mono.just(prepareSendMessage(chatId, EmojiParser.parseToUnicode(messageText)));
    }

}
