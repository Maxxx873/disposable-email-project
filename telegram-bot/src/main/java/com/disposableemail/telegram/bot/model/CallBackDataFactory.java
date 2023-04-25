package com.disposableemail.telegram.bot.model;

import com.disposableemail.telegram.bot.replier.BotReplier;
import com.disposableemail.telegram.client.disposableemail.webclient.model.Domain;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.disposableemail.telegram.bot.model.BotAction.*;
import static com.disposableemail.telegram.bot.replier.BotReplier.*;

@Component
@RequiredArgsConstructor
public class CallBackDataFactory {

    private final BotReplier botReplier;

    public List<CallbackData<AccountEntity>> getCallBackDataForAccountQuestion(AccountEntity account) {
        return Arrays.asList(
                CallbackData.<AccountEntity>builder()
                        .text(botReplier.reply(BUTTON_YES))
                        .data(account)
                        .action(CONFIRM_DELETE)
                        .build(),
                CallbackData.<AccountEntity>builder()
                        .text(botReplier.reply(BUTTON_NO))
                        .data(account)
                        .action(CANCEL)
                        .build());
    }

    public List<CallbackData<AccountEntity>> getCallBackDataForAccountsShow(AccountEntity account) {
        return Arrays.asList(
                CallbackData.<AccountEntity>builder()
                        .text(botReplier.reply(BUTTON_ACCOUNT_MESSAGES))
                        .data(account)
                        .action(GET_MESSAGES)
                        .build(),
                CallbackData.<AccountEntity>builder()
                        .text(botReplier.reply(BUTTON_ACCOUNT_DELETE))
                        .data(account)
                        .action(DELETE)
                        .build());
    }

    public CallbackData<Object> getDomainsCallbackData(Domain domain) {
        return CallbackData.builder()
                .text(domain.getDomain())
                .action(BotAction.GET_MESSAGES)
                .data(domain)
                .build();
    }
}
