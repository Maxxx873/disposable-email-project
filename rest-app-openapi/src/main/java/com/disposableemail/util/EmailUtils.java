package com.disposableemail.util;

public class EmailUtils {

    private EmailUtils() {
        throw new IllegalStateException("EmailUtils class");
    }

    public static String getDomainFromEmailAddress(String address) {
        return address.replaceAll("(.+)@","");
    }

    public static String getNameFromEmailAddress(String address) {
        return address.replaceAll("@(.+)$","");
    }

}
