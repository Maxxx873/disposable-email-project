package com.disposableemail.telegram.bot.util;

import java.util.Objects;
import java.util.regex.Pattern;

public final class EmailLoginValidation {

    private EmailLoginValidation() {
        throw new IllegalStateException("Utility class");
    }

    private static final String REGEX_PATTERN = "[A-Z-a-z0-9!#$%&'*+/=?^_`{|}~]+(?:\\.[A-Z-a-z0-9!#$%&'*+/=?^_`{|}~]+)*+";

    public static boolean isValid(String emailLogin) {
        if (Objects.equals(emailLogin, null)) {
            return false;
        } else {
            return Pattern.compile(REGEX_PATTERN)
                    .matcher(emailLogin)
                    .matches();
        }
    }
}
