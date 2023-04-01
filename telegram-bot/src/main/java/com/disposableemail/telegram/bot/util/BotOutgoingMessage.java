package com.disposableemail.telegram.bot.util;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public final class BotOutgoingMessage {

    private BotOutgoingMessage() {
        throw new IllegalStateException("Utility class");
    }

    public static SendMessage prepareSendMessage(long chatId, String textToSend) {
        var message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        return message;
    }

    public static SendMessage prepareSendMessageWithInlineKeyboard(long chatId, List<String> callbackData, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(getInlineButtons(callbackData));
        return message;
    }

    private static InlineKeyboardMarkup getInlineButtons(List<String> callbackData) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        callbackData.forEach(data -> rowList.add(getButton(data)));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private static List<InlineKeyboardButton> getButton(String callbackData) {
        var button = new InlineKeyboardButton();
        button.setText(callbackData);
        button.setCallbackData(callbackData);
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(button);
        return keyboardButtonsRow;
    }

}
