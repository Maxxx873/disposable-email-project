package com.disposableemail.telegram.bot.service.impl;

import com.disposableemail.telegram.bot.error.BotErrorHandler;
import com.disposableemail.telegram.bot.model.CallBackDataFactory;
import com.disposableemail.telegram.bot.replier.BotReplier;
import com.disposableemail.telegram.bot.service.BotAccountService;
import com.disposableemail.telegram.bot.service.BotCallbackService;
import com.disposableemail.telegram.bot.util.EmailLoginValidation;
import com.disposableemail.telegram.client.disposableemail.webclient.model.Domain;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import com.disposableemail.telegram.dao.entity.CustomerEntity;
import com.disposableemail.telegram.dao.mapper.AccountEntityMapper;
import com.disposableemail.telegram.service.AccountService;
import com.disposableemail.telegram.service.CustomerService;
import com.disposableemail.telegram.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.disposableemail.telegram.bot.handler.BotState.*;
import static com.disposableemail.telegram.bot.replier.BotReplier.*;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BotAccountServiceImpl implements BotAccountService {

    private final CustomerService customerService;
    private final BotReplier botReplier;
    private final AccountService accountService;
    private final AccountEntityMapper accountEntityMapper;
    private final TextEncryptor textEncryptor;
    private final BotCallbackService botCallbackService;
    private final CallBackDataFactory callBackDataFactory;
    private final BotErrorHandler errorHandler;
    private final EmailService emailService;

    private final Map<Long, AccountEntity> incompleteAccounts = new ConcurrentHashMap<>();

    private static final int BUILDER_CAPACITY = 100;

    @Override
    public Publisher<SendMessage> enterLogin(long chatId, String login) {
        var reply = new StringBuilder(BUILDER_CAPACITY);
        var customer = customerService.getByChatId(chatId);
        var account = incompleteAccounts.get(chatId);
        var domain = account.getDomain();
        var address = String.join("@", login.toLowerCase().trim(), domain);

        if (accountService.findByAddress(address).isPresent()) {
            customer.ifPresent(customerEntity -> customerEntity.setBotState(START));
            return Mono.just(errorHandler.handleAccountInBotExistError(chatId));
        }

        if (accountService.findByAddress(address).isEmpty() && EmailLoginValidation.isValid(login)) {
            account.setAddress(address);
            incompleteAccounts.putIfAbsent(chatId, account);
            customer.ifPresent(customerEntity -> customerEntity.setBotState(WAITING_FOR_PASSWORD_ENTRY));
            reply.append(botReplier.reply(ACCOUNTS_PASSWORD_ENTER))
                    .append(" ")
                    .append(account.getAddress());
        } else {
            customer.ifPresent(customerEntity -> customerEntity.setBotState(START));
            return Mono.just(errorHandler.handleLoginError(chatId));
        }
        customer.ifPresent(customerEntity -> customerService.save(customer.get()));
        return Mono.just(prepareSendMessage(chatId, reply.toString()));

    }

    @Override
    public Publisher<SendMessage> enterPassword(long chatId, String password) {
        var account = incompleteAccounts.get(chatId);
        account.setPassword(textEncryptor.encrypt(password));
        var customer = customerService.getByChatId(chatId);
        var reply = botReplier.reply(ACCOUNTS_ADDED) +
                " " +
                account.getAddress() +
                botReplier.reply(ADDITIONAL_HELP);
        return emailService.createAccount(accountEntityMapper.accountEntityToCredentials(account))
                .doOnSuccess(a -> botSaveAccount(chatId, account, customer))
                .map(a -> prepareSendMessage(chatId, reply))
                .onErrorResume(e -> Mono.just(errorHandler.handleErrorSendMessage(chatId, e)));
    }

    @Override
    public Publisher<SendMessage> createAccount(long chatId, Domain domain) {
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            var account = AccountEntity.builder()
                    .domain(domain.getDomain())
                    .customer(customer.get())
                    .build();
            incompleteAccounts.putIfAbsent(chatId, account);
            var reply = String.join(" ", botReplier.reply(ACCOUNTS_LOGIN_ENTER), domain.getDomain());
            customer.get().setBotState(WAITING_FOR_LOGIN_ENTRY);
            customerService.save(customer.get());
            return Mono.just(prepareSendMessage(chatId, reply));
        }
        return Mono.empty();
    }

    @Override
    public Publisher<EditMessageText> deleteAccountQuestion(Message message, AccountEntity account) {
        var customer = customerService.getByChatId(message.getChatId());
        var callbackData = callBackDataFactory.getCallBackDataForAccountQuestion(account);
        callbackData.forEach(botCallbackService::saveCallbackData);
        if (customer.isPresent()) {
            var reply = String.join(" ", botReplier.reply(ACCOUNT_DELETE_QUESTION), account.getAddress() +
                    "?");
            return Mono.just(editMessageWithInlineKeyboard(message, callbackData, reply));
        }
        return Mono.empty();
    }

    @Override
    public Publisher<EditMessageText> showAccount(Message message, AccountEntity account) {
        var customer = customerService.getByChatId(message.getChatId());
        var callbackData = callBackDataFactory.getCallBackDataForAccountsShow(account);
        callbackData.forEach(botCallbackService::saveCallbackData);
        if (customer.isPresent()) {
            return Mono.just(editMessageWithInlineKeyboard(message, callbackData, account.getAddress()));
        }
        return Mono.empty();
    }

    @Override
    public Publisher<EditMessageText> deleteAccount(Message message, AccountEntity account) {
        var reply = String.join(" ", botReplier.reply(ACCOUNT_DELETED), account.getAddress(),
                botReplier.reply(ADDITIONAL_HELP));
        return emailService.deleteAccount(accountEntityMapper.accountEntityToCredentials(account))
                .doOnSuccess(a -> botDeleteAccount(message, account))
                .map(a -> prepareEditMessage(message, reply))
                .onErrorResume(e -> Mono.just(errorHandler.handleErrorEditMessage(message, e)));
    }

    private void botDeleteAccount(Message message, AccountEntity account) {
        var customer = customerService.getByChatId(message.getChatId());
        customer.ifPresent(customerEntity -> {
            customerEntity.removeAccountByAddress(account.getAddress());
            accountService.deleteByAddress(account.getAddress());
            customerService.save(customerEntity);
        });
    }

    private void botSaveAccount(long chatId, AccountEntity account, Optional<CustomerEntity> customer) {
        accountService.createAccount(account);
        incompleteAccounts.remove(chatId);
        customer.ifPresent(customerEntity -> {
            customerEntity.addAccount(account);
            customerEntity.setBotState(START);
            customerService.save(customerEntity);
        });
    }
}
