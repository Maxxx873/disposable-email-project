package com.disposableemail.telegram.bot.util;

import com.disposableemail.telegram.bot.model.CallbackData;
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

import static com.vdurmont.emoji.EmojiParser.parseToUnicode;

public final class BotOutgoingMessage {

    private static final String HTML_PARSE_MODE = "HTML";

    private BotOutgoingMessage() {
        throw new IllegalStateException("Utility class");
    }

    public static EditMessageText prepareEditMessage(Message message, String reply) {
        return EditMessageText.builder()
                .chatId(message.getChatId())
                .parseMode(HTML_PARSE_MODE)
                .messageId(message.getMessageId())
                .text(parseToUnicode(reply))
                .build();
    }

    public static <T> EditMessageText editMessageWithInlineKeyboard(Message message, List<CallbackData<T>> callbackData, String reply) {
        return EditMessageText.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .text(parseToUnicode(reply))
                .replyMarkup(getInlineButtons(callbackData))
                .build();
    }

    public static SendDocument prepareSendDocument(long chatId, String filename, String caption, InputStream inputStream) {
        return SendDocument.builder()
                .chatId(chatId)
                .parseMode(HTML_PARSE_MODE)
                .document(new InputFile(inputStream, filename))
                .caption(parseToUnicode(caption))
                .build();
    }

    public static SendMessage prepareSendMessage(long chatId, String textToSend) {
        return SendMessage.builder()
                .chatId(chatId)
                .parseMode(HTML_PARSE_MODE)
                .text(parseToUnicode(textToSend))
                .build();
    }

    public static <T> SendMessage prepareSendMessageWithInlineKeyboard(long chatId, List<CallbackData<T>> callbackData,
                                                                       String textToSend) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(parseToUnicode(textToSend))
                .parseMode(HTML_PARSE_MODE)
                .replyMarkup(getInlineButtons(callbackData))
                .build();
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

}
