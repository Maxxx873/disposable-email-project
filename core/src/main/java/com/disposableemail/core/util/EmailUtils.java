package com.disposableemail.core.util;

import java.util.regex.Pattern;

public class EmailUtils {

    private static final String REGEX_PATTERN_VALID_MAIL = "[A-Z-a-z0-9!#$%&'*+/=?^_`{|}~]+(?:\\.[A-Z-a-z0-9!#$%&'*+/=?^_`{|}~]+)*+@([\\w-]+\\.)+[\\w-]+$";

    private EmailUtils() {
        throw new IllegalStateException("EmailUtils class");
    }

    public static String getDomainFromEmailAddress(String address) {
        validateEmail(address);
        return address.replaceAll("(.+)@", "");
    }

    public static String getNameFromEmailAddress(String address) {
        validateEmail(address);
        return address.replaceAll("@(.+)$", "");
    }

    private static void validateEmail(String address) {
        if (address == null || !Pattern.compile(REGEX_PATTERN_VALID_MAIL).matcher(address).matches()) {
            throw new IllegalArgumentException("Invalid email address: " + address);
        }
    }

}
