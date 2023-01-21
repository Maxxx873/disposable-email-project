package com.disposableemail.mail;

import com.disposableemail.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void sendMail() {
        final SimpleMailMessage simpleMail = new SimpleMailMessage();
        simpleMail.setFrom("t1@example.com");
        simpleMail.setTo("test6@example.com");
        simpleMail.setSubject("Java Mail 10");
        simpleMail.setText("Java test mail. No attachments");
        mailSender.send(simpleMail);
        assertThat(mailSender).isNotNull();
    }
}
