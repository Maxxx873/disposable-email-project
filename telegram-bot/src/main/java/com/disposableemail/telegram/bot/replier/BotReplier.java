package com.disposableemail.telegram.bot.replier;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class BotReplier {

    private final MessageSource messageSource;

    public static final String HELP = "bot.help.reply";
    public static final String ADDITIONAL_HELP = "bot.help.additional.reply";
    public static final String UNKNOWN = "bot.unknown.reply";
    public static final String ERROR = "bot.error.reply";
    public static final String OUTDATED = "bot.outdated.reply";
    public static final String REGISTERED = "bot.registered.reply";
    public static final String NOT_REGISTERED = "bot.not.registered.reply";
    public static final String ACCOUNTS_LOGIN_ENTER = "bot.accounts.login.enter.reply";
    public static final String ACCOUNTS_PASSWORD_ENTER = "bot.accounts.password.enter.reply";
    public static final String ACCOUNTS_ADDED = "bot.accounts.added.reply";
    public static final String ACCOUNTS_ERROR = "bot.accounts.error.reply";
    public static final String ACCOUNTS_ADD = "bot.accounts.add.reply";
    public static final String ACCOUNT_DELETED = "bot.accounts.deleted.reply";
    public static final String ACCOUNT_DELETE_QUESTION = "bot.accounts.delete.question";
    public static final String ACCOUNTS_NOT_FOUND = "bot.accounts.not.found.reply";


    public static final String BUTTON_ACCOUNT_MESSAGES = "bot.button.account.messages";
    public static final String BUTTON_ACCOUNT_DELETE = "bot.button.account.delete";
    public static final String BUTTON_MESSAGES_NOT_FOUND = "bot.messages.not.found.reply";
    public static final String BUTTON_YES = "bot.button.yes";
    public static final String BUTTON_NO = "bot.button.no";


    public static final String HTML_PART_NAME = "bot.file.html.name";

    public String reply(String messageKey) {
        return messageSource.getMessage(messageKey, null, Locale.getDefault());
    }
}
