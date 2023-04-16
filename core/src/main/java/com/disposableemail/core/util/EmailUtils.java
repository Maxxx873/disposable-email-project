package com.disposableemail.core.util;

import java.util.regex.Pattern;

public class EmailUtils {

 private static final String REGEX_PATTERN_VALID_MAIL = "[A-Z-a-z0-9!#$%&'*+/=?^_`{|}~]+(?:\\.[A-Z-a-z0-9!#$%&'*+/=?^_`{|}~]+)*+@([\\w-]+\\.)+[\\w-]+$";

    private EmailUtils() {
        throw new IllegalStateException("EmailUtils class");
    }

    public static String getDomainFromEmailAddress(String address) {
        if (isInvalidEmail(address)) {
            throw new IllegalStateException("Address is invalid");
        }
        return address.replaceAll("(.+)@", "");

    }

    private static Boolean isInvalidEmail(String address) {
        if (address == null) {
            return true;
        } else {
            return !Pattern.compile(REGEX_PATTERN_VALID_MAIL)
                    .matcher(address)
                    .matches();
        }
    }

    public static String getNameFromEmailAddress(String address) {
        if (isInvalidEmail(address)) {
            throw new IllegalStateException("Address is invalid");
        }
        return address.replaceAll("@(.+)$", "");
    }

}
