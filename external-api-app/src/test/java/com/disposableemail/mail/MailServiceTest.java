package com.disposableemail.mail;

import com.disposableemail.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
class MailServiceTest {

    private JavaMailSenderImpl mailSender;

    @BeforeEach
    void setUp() {
        this.mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        mailSender.setUsername("t1@example.com");
        mailSender.setPassword("password");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
    }

    @Disabled
    @RepeatedTest(2)
    void shouldSendMail() {
        final SimpleMailMessage simpleMail = new SimpleMailMessage();
        simpleMail.setFrom("t6@example.com");
        simpleMail.setTo("ffg@example.com");
        simpleMail.setSubject("Java Mail");
        simpleMail.setText("Test1");
        mailSender.send(simpleMail);
        assertThat(mailSender).isNotNull();
    }
}
