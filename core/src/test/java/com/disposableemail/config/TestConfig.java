package com.disposableemail.config;

import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.event.ApplicationEventListener;
import com.disposableemail.core.event.producer.EventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fridujo.rabbitmq.mock.compatibility.MockConnectionFactoryFactory;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
@ActiveProfiles("test")
@MockBeans({
        @MockBean(ReactiveJwtDecoder.class),
        @MockBean(EventProducer.class),
        @MockBean(Channel.class),
        @MockBean(ApplicationEventListener.class),
        @MockBean(LettuceConnectionFactory.class),
        @MockBean(WebClient.class)
})
public class TestConfig {

    @Autowired
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Bean
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory(
                MockConnectionFactoryFactory.build());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public ReactiveRedisTemplate<String, DomainEntity> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<DomainEntity> valueSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, DomainEntity.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, DomainEntity> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);
        RedisSerializationContext<String, DomainEntity> context = builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

}