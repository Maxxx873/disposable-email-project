package com.disposableemail.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import com.disposableemail.core.event.ApplicationEventListener;
import com.disposableemail.core.event.producer.EventProducer;
import com.github.fridujo.rabbitmq.mock.compatibility.MockConnectionFactoryFactory;
import com.rabbitmq.client.Channel;

@TestConfiguration
@ActiveProfiles("test")
@MockBeans({
        @MockBean(ReactiveJwtDecoder.class),
        @MockBean(EventProducer.class),
        @MockBean(Channel.class),
        @MockBean(ApplicationEventListener.class),
        @MockBean(WebClient.class)
})
public class TestConfig {

    @Bean
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory(
                MockConnectionFactoryFactory.build());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

}