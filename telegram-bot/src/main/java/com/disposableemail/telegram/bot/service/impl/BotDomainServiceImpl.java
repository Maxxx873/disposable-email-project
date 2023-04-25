package com.disposableemail.telegram.bot.service.impl;

import com.disposableemail.telegram.bot.error.BotErrorHandler;
import com.disposableemail.telegram.bot.model.CallBackDataFactory;
import com.disposableemail.telegram.bot.replier.BotReplier;
import com.disposableemail.telegram.bot.service.BotCallbackService;
import com.disposableemail.telegram.bot.service.BotDomainService;
import com.disposableemail.telegram.service.CustomerService;
import com.disposableemail.telegram.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import static com.disposableemail.telegram.bot.handler.BotState.WAITING_FOR_DOMAIN_CHOICE;
import static com.disposableemail.telegram.bot.replier.BotReplier.ACCOUNTS_ADD;
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
    private final BotErrorHandler errorHandler;
    private final CallBackDataFactory callBackDataFactory;

    private static final int DEFAULT_SIZE = 10;

    @Override
    public Publisher<?> showDomains(long chatId) {
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            var messageText = botReplier.reply(ACCOUNTS_ADD);
            customer.get().setBotState(WAITING_FOR_DOMAIN_CHOICE);
            customerService.save(customer.get());
            return emailService.getDomains(DEFAULT_SIZE)
                    .map(callBackDataFactory::getDomainsCallbackData)
                    .collectList()
                    .doOnSuccess(domains -> domains.forEach(botCallbackService::saveCallbackData))
                    .map(callbackData ->
                            prepareSendMessageWithInlineKeyboard(customer.get().getChatId(), callbackData, messageText))
                    .onErrorResume(e -> Mono.just(errorHandler.handleErrorSendMessage(chatId, e)));
        } else {
            return Mono.just(errorHandler.handleNotRegisteredCustomerError(chatId));
        }
    }
}
