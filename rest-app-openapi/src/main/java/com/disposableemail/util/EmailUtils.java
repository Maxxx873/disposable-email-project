package com.disposableemail.util;

public class EmailUtils {

    private EmailUtils() {
        throw new IllegalStateException("EmailUtils class");
    }

    public static String getDomainFromEmailAddress(String address) {
        return address.substring(address.indexOf('@') + 1);
    }

    public static String getNameFromEmailAddress(String address) {
        return address.substring(0, address.indexOf('@'));
    }

}
