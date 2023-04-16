package com.disposableemail.telegram.bot.service.impl;

import com.disposableemail.telegram.bot.model.dto.MessageDto;
import com.disposableemail.telegram.bot.model.mapper.MessageMapper;
import com.disposableemail.telegram.bot.replier.BotReplier;
import com.disposableemail.telegram.bot.service.BotMessageService;
import com.disposableemail.telegram.bot.util.BotFileHelper;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import com.disposableemail.telegram.dao.mapper.AccountEntityMapper;
import com.disposableemail.telegram.service.CustomerService;
import com.disposableemail.telegram.service.EmailService;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Mono;

import static com.disposableemail.telegram.bot.replier.BotReplier.*;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareSendDocument;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareSendMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotMessageServiceImpl implements BotMessageService {

    private final CustomerService customerService;
    private final BotReplier botReplier;
    private final EmailService emailService;
    private final AccountEntityMapper accountEntityMapper;
    private final MessageMapper messageMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @Override
    public Publisher<SendMessage> getHtmlPart(long chatId, MessageDto messageDto) {
        return null;
    }

    @Override
    public Publisher<?> showMessages(long chatId, AccountEntity account) {
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            String reply = botReplier.reply(BUTTON_MESSAGES_NOT_FOUND);
            String answer = EmojiParser.parseToUnicode(reply);
            return emailService.getMessages(accountEntityMapper.accountEntityToCredentials(account),
                            DEFAULT_PAGE, DEFAULT_SIZE)
                    .map(messageMapper::messageToDto)
                    .map(messageDto -> {
                        if (messageDto.getHtml().isEmpty()) {
                            return prepareSendMessage(customer.get().getChatId(), messageDto.toString());
                        } else {
                            return prepareSendDocument(customer.get().getChatId(), botReplier.reply(HTML_PART_NAME),
                                    messageDto.toString(), BotFileHelper.getHtmlPartasInputStream(messageDto.getHtml()));
                        }
                    })
                    .defaultIfEmpty(prepareSendMessage(chatId, answer));

        } else {
            return getDefaultMessage(chatId);
        }
    }

    private Mono<SendMessage> getDefaultMessage(long chatId) {
        String messageText = botReplier.reply(NOT_REGISTERED);
        return Mono.just(prepareSendMessage(chatId, EmojiParser.parseToUnicode(messageText)));
    }
}
