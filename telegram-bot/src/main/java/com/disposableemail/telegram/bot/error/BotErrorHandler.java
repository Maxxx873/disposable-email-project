package com.disposableemail.telegram.bot.error;

import com.disposableemail.telegram.bot.replier.BotReplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.disposableemail.telegram.bot.replier.BotReplier.*;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareEditMessage;
import static com.disposableemail.telegram.bot.util.BotOutgoingMessage.prepareSendMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotErrorHandler {

    private final BotReplier botReplier;

    public SendMessage handleErrorSendMessage(long chatId, Throwable throwable) {
        log.error("Error handling for customerId: {} | {}:{}", chatId, throwable.getClass().getSimpleName(), throwable.getMessage());
        return getPreparedMessage(chatId, getErrorText(throwable));
    }

    public EditMessageText handleErrorEditMessage(Message message, Throwable throwable) {
        log.error("Error handling for customerId: {} | {}:{}", message.getMessageId(), throwable.getClass().getSimpleName(),
                throwable.getMessage());
        return getEditTextMessage(message, getErrorText(throwable));
    }

    public SendMessage handleNotRegisteredCustomerError(long chatId) {
        return prepareSendMessage(chatId, botReplier.reply(NOT_REGISTERED));
    }

    public SendMessage handleLoginError(long chatId) {
        return prepareSendMessage(chatId, botReplier.reply(ERROR) +
                "<i>Your login contains invalid characters!</i>" + botReplier.reply(ADDITIONAL_HELP));
    }

    public SendMessage handleAccountInBotExistError(long chatId) {
        return prepareSendMessage(chatId, botReplier.reply(ERROR) +
                "<i>This account already exists!</i>" + botReplier.reply(ADDITIONAL_HELP));
    }

    private SendMessage getPreparedMessage(long chatId, String messageText) {
        return prepareSendMessage(chatId, botReplier.reply(ERROR) + messageText +
                botReplier.reply(ADDITIONAL_HELP));
    }

    private EditMessageText getEditTextMessage(Message message, String messageText) {
        return prepareEditMessage(message, botReplier.reply(ERROR) + messageText +
                botReplier.reply(ADDITIONAL_HELP));
    }

    private String getErrorText(Throwable throwable) {
        return switch (throwable) {
            case WebClientRequestException e -> "<i>Mail server connection refused! Please, try again later.</i>";
            case WebClientResponseException.Unauthorized e ->  "<i>Unauthorized! Your password is incorrect.</i>";
            case WebClientResponseException.Conflict e -> "<i>This account is already registered on the mail server!.</i>";
            default -> "<i>Something wrong!</i>";
        };
    }

}
