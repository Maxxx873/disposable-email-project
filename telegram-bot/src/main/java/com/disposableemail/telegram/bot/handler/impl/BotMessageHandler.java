package com.disposableemail.telegram.bot.handler.impl;

import com.disposableemail.telegram.bot.handler.BotState;
import com.disposableemail.telegram.bot.service.BotAccountService;
import com.disposableemail.telegram.bot.service.BotCustomerService;
import com.disposableemail.telegram.bot.service.BotDomainService;
import com.disposableemail.telegram.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class BotMessageHandler {

    private final BotCustomerService botCustomerService;
    private final BotDomainService botDomainService;
    private final BotAccountService botAccountService;
    private final CustomerService customerService;

    private BotState botState;

    public Publisher<?> processMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        var customer = customerService.getByChatId(chatId);
        customer.ifPresent(customerEntity -> botState = customerEntity.getBotState());

        return switch (update.getMessage().getText()) {
            case "/start" -> botCustomerService.registerCustomer(chatId, getCustomerName(update));
            case "/new" -> botDomainService.showDomains(chatId);
            case "/list" -> botCustomerService.showAccounts(chatId);
            case "/help" -> botCustomerService.showHelp(chatId);
            default -> {
                var message = update.getMessage().getText();
                yield choiceAnswer(chatId, message);
            }
        };
    }

    private Publisher<SendMessage> choiceAnswer(long chatId, String message) {
        return switch (botState) {
            case WAITING_FOR_LOGIN_ENTRY -> botAccountService.enterLogin(chatId, message);
            case WAITING_FOR_PASSWORD_ENTRY -> botAccountService.enterPassword(chatId, message);
            default -> botCustomerService.showAnswer(chatId, message);
        };
    }

    private static String getCustomerName(Update update) {
        var customerName = "";
        if (update.hasMessage()) {
            var firstName = Optional.ofNullable(update.getMessage().getChat().getFirstName()).orElse("");
            var lastName = Optional.ofNullable(update.getMessage().getChat().getLastName()).orElse("");
            customerName = String.join(" ", firstName, lastName);
        }
        return customerName.trim();
    }
}

