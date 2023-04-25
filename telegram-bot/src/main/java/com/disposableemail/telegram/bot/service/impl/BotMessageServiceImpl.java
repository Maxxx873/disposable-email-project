package com.disposableemail.telegram.bot.service.impl;

import com.disposableemail.telegram.bot.error.BotErrorHandler;
import com.disposableemail.telegram.bot.model.dto.MessageDto;
import com.disposableemail.telegram.bot.model.mapper.MessageMapper;
import com.disposableemail.telegram.bot.replier.BotReplier;
import com.disposableemail.telegram.bot.service.BotMessageService;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import com.disposableemail.telegram.dao.mapper.AccountEntityMapper;
import com.disposableemail.telegram.service.CustomerService;
import com.disposableemail.telegram.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.disposableemail.telegram.bot.handler.BotState.START;
import static com.disposableemail.telegram.bot.replier.BotReplier.BUTTON_MESSAGES_NOT_FOUND;
import static com.disposableemail.telegram.bot.replier.BotReplier.HTML_PART_NAME;
import static com.disposableemail.telegram.bot.util.BotFileHelper.getHtmlPartsInputStream;
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
    private final BotErrorHandler errorHandler;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @Override
    public Publisher<?> showMessages(long chatId, AccountEntity account) {
        var customer = customerService.getByChatId(chatId);
        if (customer.isPresent()) {
            customer.get().setBotState(START);
            customerService.save(customer.get());
            return emailService.getMessages(accountEntityMapper.accountEntityToCredentials(account),
                            DEFAULT_PAGE, DEFAULT_SIZE)
                    .map(messageMapper::messageToDto)
                    .collectSortedList(Comparator.reverseOrder())
                    .map(messagesDto -> getPreparedMessages(chatId, account.getAddress(), messagesDto))
                    .onErrorResume(e -> Mono.just(Collections.singletonList(errorHandler.handleErrorSendMessage(chatId, e))));
        } else {
            return Mono.just(errorHandler.handleNotRegisteredCustomerError(chatId));
        }
    }

    private List<Object> getPreparedMessages(long chatId, String address, List<MessageDto> messagesDto) {
        var reply = String.join(" ", botReplier.reply(BUTTON_MESSAGES_NOT_FOUND), address);
        var result = new ArrayList<>();
        messagesDto.forEach(messageDto -> {
            if (messageDto.getHtml().isEmpty()) {
                result.add(prepareSendMessage(chatId, messageDto.getSendMessageText()));
            } else {
                result.add(prepareSendDocument(chatId, botReplier.reply(HTML_PART_NAME),
                        messageDto.getSendDocumentCaption(), getHtmlPartsInputStream(messageDto.getHtml())));
            }
        });
        if (result.isEmpty()) {
            result.add(prepareSendMessage(chatId, reply));
        }
        return result;
    }
}
