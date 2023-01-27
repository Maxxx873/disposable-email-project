package com.disposableemail.config;

import com.disposableemail.dao.repository.search.MessageElasticsearchRepository;
import com.disposableemail.event.EventProducer;
import com.rabbitmq.client.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Properties;

@TestConfiguration
@ActiveProfiles("test")
@MockBeans({@MockBean(MessageElasticsearchRepository.class),
        @MockBean(ReactiveJwtDecoder.class),
        @MockBean(EventProducer.class),
        @MockBean(Channel.class)})
public class TestConfig {

    @Autowired
    private MessageElasticsearchRepository messageElasticsearchRepository;
    @Autowired
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost("localhost");
        mailSender.setPort(25);

        mailSender.setUsername("t1@example.com");
        mailSender.setPassword("password");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
