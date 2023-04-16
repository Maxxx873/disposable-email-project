package com.disposableemail.telegram.bot.util;

import com.disposableemail.telegram.bot.model.CallbackData;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class BotOutgoingMessage {

    private static final int MAX_CAPTION_SIZE = 1024;
    private static final int MAX_TEXT_SIZE = 4096;
    private static final String END_MESSAGE = "...";


    private BotOutgoingMessage() {
        throw new IllegalStateException("Utility class");
    }

    public static EditMessageText editMessage(Message message, String reply) {
        return EditMessageText.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .text(reply)
                .build();
    }

    public static SendDocument prepareSendDocument(long chatId, String filename, String caption, InputStream inputStream) {
        var sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        sendDocumentRequest.setDocument(new InputFile(inputStream, filename));
        sendDocumentRequest.setCaption(truncateCaption(caption));
        return sendDocumentRequest;
    }

    public static SendMessage prepareSendMessage(long chatId, String textToSend) {
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        return message;
    }

    public static <T> SendMessage prepareSendMessageWithInlineKeyboard(long chatId, List<CallbackData<T>> callbackData, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(truncateText(textToSend));
        message.setReplyMarkup(getInlineButtons(callbackData));
        return message;
    }

    private static <T> InlineKeyboardMarkup getInlineButtons(List<CallbackData<T>> callbackData) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        callbackData.forEach(callbackDataDto -> rowList.add(getButton(callbackDataDto)));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private static <T> List<InlineKeyboardButton> getButton(CallbackData<T> callbackData) {
        var button = new InlineKeyboardButton();
        button.setText(callbackData.getText());
        button.setCallbackData(callbackData.getId());
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(button);
        return keyboardButtonsRow;
    }

    private static String truncateCaption(String caption) {
        if (caption.length() > MAX_CAPTION_SIZE) {
            return StringUtils.truncate(caption, MAX_CAPTION_SIZE - END_MESSAGE.length()) + END_MESSAGE;
        }
        return caption;
    }

    private static String truncateText(String text) {
        if (text.length() > MAX_TEXT_SIZE) {
            return StringUtils.truncate(text, MAX_TEXT_SIZE - END_MESSAGE.length()) + END_MESSAGE;
        }
        return text;
    }
}
