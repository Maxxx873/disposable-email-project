package com.disposableemail.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailUtilsTest {

    @Test
    void testGetDomainFromEmailAddress() {
        var address = "john.doe@example.com";
        var expectedDomain = "example.com";
        var actualDomain = EmailUtils.getDomainFromEmailAddress(address);
        assertEquals(expectedDomain, actualDomain);
    }

    @Test
    void testGetDomainFromInvalidEmailAddress() {
        var address = "invalid-email-address";
        assertThrows(IllegalArgumentException.class, () -> EmailUtils.getDomainFromEmailAddress(address), "Expected IllegalArgumentException to be thrown");
    }

    @Test
    void testGetNameFromEmailAddress() {
        var address = "john.doe@example.com";
        var expectedName = "john.doe";
        var actualName = EmailUtils.getNameFromEmailAddress(address);
        assertEquals(expectedName, actualName);
    }

    @Test
    void testGetNameFromInvalidEmailAddress() {
        var address = "invalid-email-address";
        assertThrows(IllegalArgumentException.class, () -> EmailUtils.getNameFromEmailAddress(address), "Expected IllegalArgumentException to be thrown");
    }
}