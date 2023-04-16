package com.disposableemail.telegram.bot.service.impl;

import com.disposableemail.telegram.bot.event.AccountCreationEventPublisher;
import com.disposableemail.telegram.bot.event.AccountDeletionEventPublisher;
import com.disposableemail.telegram.bot.replier.BotReplier;
import com.disposableemail.telegram.bot.service.BotAccountService;
import com.disposableemail.telegram.bot.util.EmailLoginValidation;
import com.disposableemail.telegram.client.disposableemail.webclient.model.Domain;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import com.disposableemail.telegram.dao.mapper.AccountEntityMapper;
import com.disposableemail.telegram.service.AccountService;
import com.disposableemail.telegram.service.CustomerService;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.disposableemail.telegram.bot.handler.BotState.*;
import static com.disposableemail.telegram.bot.replier.BotReplier.*;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareSendMessage;

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
    private final AccountCreationEventPublisher accountCreationEventPublisher;
    private final AccountDeletionEventPublisher accountDeletionEventPublisher;

    private final Map<Long, AccountEntity> incompleteAccounts = new ConcurrentHashMap<>();

    private static final int BUILDER_CAPACITY = 100;

    @Override
    public Publisher<SendMessage> enterLogin(long chatId, String login) {
        StringBuilder reply = new StringBuilder(BUILDER_CAPACITY);
        var customer = customerService.getByChatId(chatId);
        var account = incompleteAccounts.get(chatId);
        String domain = account.getDomain();
        String address = String.join("@", login.toLowerCase().trim(), domain);

        if (accountService.findByAddress(address).isEmpty() && EmailLoginValidation.isValid(login)) {
            account.setAddress(address);
            incompleteAccounts.put(chatId, account);
            customer.ifPresent(customerEntity -> customerEntity.setBotState(WAITING_FOR_PASSWORD_ENTRY));
            reply.append(botReplier.reply(ACCOUNTS_PASSWORD_ENTER))
                    .append(" ")
                    .append(account.getAddress());
        } else {
            customer.ifPresent(customerEntity -> customerEntity.setBotState(START));
            reply.append(botReplier.reply(ACCOUNTS_ERROR));
        }
        customer.ifPresent(customerEntity -> customerService.save(customer.get()));
        return Mono.just(prepareSendMessage(chatId, EmojiParser.parseToUnicode(reply.toString())));

    }

    @Override
    public Publisher<SendMessage> enterPassword(long chatId, String password) {
        StringBuilder reply = new StringBuilder(BUILDER_CAPACITY);
        var account = incompleteAccounts.get(chatId);
        var customer = customerService.getByChatId(chatId);
        account.setPassword(textEncryptor.encrypt(password));
        accountService.createAccount(account);
        incompleteAccounts.remove(chatId);
        customer.ifPresent(customerEntity -> {
            customerEntity.addAccount(account);
            customerEntity.setBotState(START);
            customerService.save(customerEntity);
        });
        accountCreationEventPublisher.publish(accountEntityMapper.accountEntityToCredentials(account));
        reply.append(botReplier.reply(ACCOUNTS_ADDED))
                .append(" ")
                .append(account.getAddress())
                .append(botReplier.reply(ADDITIONAL_HELP));
        return Mono.just(prepareSendMessage(chatId, EmojiParser.parseToUnicode(reply.toString())));

    }

    @Override
    public Publisher<SendMessage> createAccount(long chatId, Domain domain) {
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            var account = AccountEntity.builder()
                    .domain(domain.getDomain())
                    .customer(customer.get())
                    .build();
            incompleteAccounts.put(chatId, account);
            String reply = String.join(" ", botReplier.reply(ACCOUNTS_LOGIN_ENTER), domain.getDomain());
            customer.get().setBotState(WAITING_FOR_LOGIN_ENTRY);
            customerService.save(customer.get());
            return Mono.just(prepareSendMessage(chatId, reply));
        }
        return Mono.empty();
    }

    @Override
    public Publisher<SendMessage> deleteAccount(long chatId, AccountEntity account) {
        var customer = customerService.getByChatId(chatId);
        customer.ifPresent(customerEntity -> {
            customerEntity.removeAccountByAddress(account.getAddress());
            accountService.deleteByAddress(account.getAddress());
            customerService.save(customerEntity);
        });
        accountDeletionEventPublisher.publish(accountEntityMapper.accountEntityToCredentials(account));
        String reply = String.join(" ", botReplier.reply(ACCOUNT_DELETED), account.getAddress(),
                botReplier.reply(ADDITIONAL_HELP));
        return Mono.just(prepareSendMessage(chatId, EmojiParser.parseToUnicode(reply)));
    }
}
