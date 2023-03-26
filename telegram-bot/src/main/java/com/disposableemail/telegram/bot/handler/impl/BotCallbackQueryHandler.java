package com.disposableemail.telegram.bot.handler.impl;

import com.disposableemail.telegram.bot.BotMessageSource;
import com.disposableemail.telegram.bot.handler.BotState;
import com.disposableemail.telegram.bot.service.BotService;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import com.disposableemail.telegram.dao.entity.CustomerEntity;
import com.disposableemail.telegram.service.AccountService;
import com.disposableemail.telegram.service.CustomerService;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareSendMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotCallbackQueryHandler {
    private final CustomerService customerService;
    private final BotMessageSource botMessageSource;
    private final AccountService accountService;
    private final BotService botService;

    @Transactional
    public Publisher<SendMessage> processCallbackQuery(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        String text = callbackQuery.getMessage().getText();
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent() && customer.get().getBotState().equals(BotState.WAITING_FOR_DOMAIN_CHOICE)) {
            updateCustomer(customer.get(), getNewAccount(callbackQuery, customer.get()));
            String reply = botMessageSource.getMessage("bot.accounts.login.enter.reply") + " " + text;
            return Mono.just(prepareSendMessage(chatId, reply));
        }
        if (customer.isPresent() && customer.get().getBotState().equals(BotState.WAITING_FOR_ACCOUNT_CHOICE)) {
            if (Objects.equals(callbackQuery.getData(), botMessageSource.getMessage("bot.button.account.messages"))) {
                return botService.showMessages(chatId, text);
            }
            if (Objects.equals(callbackQuery.getData(), botMessageSource.getMessage("bot.button.account.delete"))) {
                customer.get().removeAccountByAddress(text);
                accountService.deleteByAddress(text);
                customerService.save(customer.get());
                String reply = botMessageSource.getMessage("bot.accounts.deleted.reply") + " " + text;
                String answer = EmojiParser.parseToUnicode(reply);
                return Mono.just(prepareSendMessage(chatId, answer));
            }
        }
        return Mono.empty();
    }

    private void updateCustomer(CustomerEntity customer, AccountEntity account) {
        customer.addAccount(account);
        customer.setBotState(BotState.WAITING_FOR_LOGIN_ENTRY);
        customerService.save(customer);
    }

    private AccountEntity getNewAccount(CallbackQuery callbackQuery, CustomerEntity customer) {
        var account = AccountEntity.builder()
                .domain(callbackQuery.getMessage().getText())
                .customer(customer)
                .build();
        return accountService.createAccount(account);
    }
}
