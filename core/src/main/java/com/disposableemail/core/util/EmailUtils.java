package com.disposableemail.core.util;

import java.util.Objects;

public class EmailUtils {

    private EmailUtils() {
        throw new IllegalStateException("EmailUtils class");
    }

    public static String getDomainFromEmailAddress(String address) {
        if (Objects.equals(address, null)) {
            throw new IllegalStateException("Address is null");
        }
        return address.replaceAll("(.+)@", "");

    }

    public static String getNameFromEmailAddress(String address) {
        if (Objects.equals(address, null)) {
            throw new IllegalStateException("Address is null");
        }
        return address.replaceAll("@(.+)$", "");
    }

}
