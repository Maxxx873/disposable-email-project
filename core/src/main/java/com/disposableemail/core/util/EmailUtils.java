package com.disposableemail.core.util;

import java.util.Objects;

public class EmailUtils {

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
        return address == null || !address.contains("@");
    }

    public static String getNameFromEmailAddress(String address) {
        if (isInvalidEmail(address)) {
            throw new IllegalStateException("Address is invalid");
        }
        return address.replaceAll("@(.+)$", "");
    }

}
