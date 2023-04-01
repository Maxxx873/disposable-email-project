package com.disposableemail.telegram.bot.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MailLoginValidationTest {
    private String mailLogin;

    @Test
    void shouldValidMailLogin() {
        mailLogin = "Username345_";
        assertThat(EmailLoginValidation.isValid(mailLogin)).isTrue();
    }

    @Test
    void shouldNotValidMailLogin() {
        mailLogin = ".username";
        assertThat(EmailLoginValidation.isValid(mailLogin)).isFalse();
    }

    @Test
    void shouldNotValidIfMailLoginIsNull() {
        mailLogin = null;
        assertThat(EmailLoginValidation.isValid(mailLogin)).isFalse();
    }

}