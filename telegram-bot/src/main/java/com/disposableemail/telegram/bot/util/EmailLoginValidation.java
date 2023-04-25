package com.disposableemail.telegram.bot.util;

import java.util.Objects;
import java.util.regex.Pattern;

public final class EmailLoginValidation {

    private static final String REGEX_PATTERN = "[\\w!#$%&'*+/=?^`{|}~]+(?:\\.[\\w!#$%&'*+/=?^`{|}~]+)*+";


    private EmailLoginValidation() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isValid(String emailLogin) {
        if (Objects.isNull(emailLogin)) {
            return false;
        } else {
            return Pattern.compile(REGEX_PATTERN)
                    .matcher(emailLogin)
                    .matches();
        }
    }
}
