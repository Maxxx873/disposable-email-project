package com.disposableemail.telegram.bot.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class MailLoginValidationTest {

    @Test
    void shouldValidMailLogin() {
        String mailLogin = "Username_345_";
        assertThat(EmailLoginValidation.isValid(mailLogin)).isTrue();
    }

    @Test
    void shouldNotValidIfMailLoginIsNull() {
        assertThat(EmailLoginValidation.isValid(null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ".username",
            "",
            "username@username.com"
    })
    void shouldNotValidMailLogin(String mailLogin) {
        assertThat(EmailLoginValidation.isValid(mailLogin)).isFalse();
    }

}