package com.disposableemail.telegram.bot.service;

import com.disposableemail.telegram.bot.BotMessageSource;
import com.disposableemail.telegram.bot.event.AccountCreationEventPublisher;
import com.disposableemail.telegram.bot.handler.BotState;
import com.disposableemail.telegram.bot.util.EmailLoginValidation;
import com.disposableemail.telegram.client.disposableemail.webclient.model.Credentials;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import com.disposableemail.telegram.service.AccountService;
import com.disposableemail.telegram.service.AuthService;
import com.disposableemail.telegram.service.CustomerService;
import com.disposableemail.telegram.service.EmailService;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareSendMessage;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareSendMessageWithInlineKeyboard;
import static com.disposableemail.telegram.dao.entity.CustomerEntity.getNewCustomer;

@Slf4j
@Service
@Transactional(rollbackFor = DataIntegrityViolationException.class)
@RequiredArgsConstructor
public class BotService {

    private final EmailService emailService;
    private final CustomerService customerService;
    private final AuthService authService;
    private final BotMessageSource botMessageSource;
    private final TextEncryptor encryptor;
    private final AccountService accountService;
    private final AccountCreationEventPublisher accountCreationEventPublisher;

    private static final String BOT_NOT_REGISTERED_REPLY = "bot.not.registered.reply";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;


    public Mono<SendMessage> registerCustomer(Update update) {
        long chatId = update.getMessage().getChatId();
        String name = update.getMessage().getChat().getFirstName() + " " + update.getMessage().getChat().getLastName();
        String messageText = botMessageSource.getMessage("bot.registered.reply");
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            customer.get().setBotState(BotState.START);
            customerService.save(customer.get());
        } else {
            customerService.save(getNewCustomer(chatId, name));
        }
        return Mono.just(prepareSendMessage(chatId, EmojiParser.parseToUnicode(messageText + " " + name)));
    }

    public Publisher<SendMessage> showDomains(Update update) {
        long chatId = update.getMessage().getChatId();
        String messageText;
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            messageText = botMessageSource.getMessage("bot.button.account.add");
            customer.get().setBotState(BotState.WAITING_FOR_DOMAIN_CHOICE);
            customerService.save(customer.get());
            var messageTexts = Collections.singletonList(messageText);
            return emailService.getDomains(DEFAULT_SIZE)
                    .map(message -> prepareSendMessageWithInlineKeyboard(customer.get().getChatId(), messageTexts, message));
        } else {
            return getDefaultMessage(BOT_NOT_REGISTERED_REPLY, chatId);
        }
    }

    public Publisher<SendMessage> showMessages(long chatId, String address) {
        var account = accountService.findByAddress(address);
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent() && account.isPresent()) {
            authService.authenticate(account.get()).block();
            String reply = botMessageSource.getMessage("bot.messages.not.found.reply");
            String answer = EmojiParser.parseToUnicode(reply);
            return emailService.getMessages(DEFAULT_PAGE, DEFAULT_SIZE)
                    .map(message -> prepareSendMessage(customer.get().getChatId(), message))
                    .defaultIfEmpty(prepareSendMessage(chatId, answer));
        } else {
            return getDefaultMessage(BOT_NOT_REGISTERED_REPLY, chatId);
        }
    }

    public Publisher<SendMessage> showHelp(Update update) {
        long chatId = update.getMessage().getChatId();
        return getDefaultMessage("bot.help.reply", chatId);
    }

    public Publisher<SendMessage> showAccounts(Update update) {
        long chatId = update.getMessage().getChatId();
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            customer.get().setBotState(BotState.WAITING_FOR_ACCOUNT_CHOICE);
            customerService.save(customer.get());
            String reply = botMessageSource.getMessage("bot.accounts.not.found.reply");
            String answer = EmojiParser.parseToUnicode(reply);
            List<String> callbackData =
                    Arrays.asList(botMessageSource.getMessage("bot.button.account.messages"),
                            botMessageSource.getMessage("bot.button.account.delete"));
            return Flux.fromStream(customer.get().getAccounts().stream()
                            .map(accountEntity -> prepareSendMessageWithInlineKeyboard(customer.get().getChatId(), callbackData, accountEntity.getAddress())))
                    .defaultIfEmpty(prepareSendMessage(chatId, answer));
        } else {
            return getDefaultMessage(BOT_NOT_REGISTERED_REPLY, chatId);
        }
    }

    public Publisher<SendMessage> showAnswer(Update update) {
        long chatId = update.getMessage().getChatId();
        var customer = customerService.getByChatId(chatId);
        var message = update.getMessage().getText();
        StringBuilder reply = new StringBuilder(100);
        if (customer.isPresent()) {
            switch (customer.get().getBotState()) {
                case WAITING_FOR_LOGIN_ENTRY -> {
                    var account = customer.get().getAccounts().stream()
                            .filter(accountEntity -> Objects.equals(accountEntity.getPassword(), null) && !Objects.equals(accountEntity.getCreatedDate(), null))
                            .min(Comparator.comparing(AccountEntity::getCreatedDate));
                    String domain = account.map(AccountEntity::getDomain).orElse("");
                    if (account.isPresent()) {
                        String address = message.toLowerCase().trim() + '@' + domain;
                        if (accountService.findByAddress(address).isEmpty() && EmailLoginValidation.isValid(message)) {
                            account.get().setAddress(address);
                            customer.get().setBotState(BotState.WAITING_FOR_PASSWORD_ENTRY);
                            customerService.save(customer.get());
                            reply.append(botMessageSource.getMessage("bot.accounts.password.enter.reply")).append(" ").append(domain);
                        } else {
                            customer.get().removeAccount(account.get());
                            customer.get().setBotState(BotState.START);
                            customerService.save(customer.get());
                            reply.append(botMessageSource.getMessage("bot.accounts.error.reply"));
                        }
                    }
                }
                case WAITING_FOR_PASSWORD_ENTRY -> {
                    var account = customer.get()
                            .getAccounts().stream()
                            .filter(accountEntity -> Objects.equals(accountEntity.getPassword(), null) && !Objects.equals(accountEntity.getCreatedDate(), null))
                            .min(Comparator.comparing(AccountEntity::getCreatedDate));
                    account.ifPresent(accountEntity -> {
                        publishCreationAccountEvent(message, accountEntity);
                        accountEntity.setPassword(encryptor.encrypt(message));
                    });
                    customer.get().setBotState(BotState.START);
                    customerService.save(customer.get());
                    String address = account.map(AccountEntity::getAddress).orElse("");
                    reply.append(botMessageSource.getMessage("bot.accounts.added.reply")).append(" ").append(address);
                }
                default -> reply.append(botMessageSource.getMessage("bot.unknown.reply"));
            }
        }
        return Mono.just(prepareSendMessage(chatId, EmojiParser.parseToUnicode(reply.toString())));
    }

    private void publishCreationAccountEvent(String message, AccountEntity account) {
        var credentials = new Credentials();
        credentials.setAddress(account.getAddress());
        credentials.setPassword(message);
        accountCreationEventPublisher.publish(credentials);
    }

    private Mono<SendMessage> getDefaultMessage(String messageKey, long chatId) {
        String messageText = botMessageSource.getMessage(messageKey);
        return Mono.just(prepareSendMessage(chatId, EmojiParser.parseToUnicode(messageText)));
    }

}
