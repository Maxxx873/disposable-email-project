package com.disposableemail.telegram.bot.handler;

public enum BotState {

    START,
    WAITING_FOR_DOMAIN_CHOICE,
    WAITING_FOR_LOGIN_ENTRY,
    WAITING_FOR_PASSWORD_ENTRY,
    WAITING_FOR_ACCOUNT_CHOICE,
    WAITING_FOR_MESSAGE_CHOICE
}
