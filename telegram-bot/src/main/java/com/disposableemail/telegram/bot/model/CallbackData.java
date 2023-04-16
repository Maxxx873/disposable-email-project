package com.disposableemail.telegram.bot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallbackData<T> {

    private String id;
    private long chatId;
    private BotAction action;
    private String text;
    private T data;
}
