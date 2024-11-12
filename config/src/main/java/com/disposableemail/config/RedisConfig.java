package com.disposableemail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.disposableemail.core.dao.entity.DomainEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RedisConfig {

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
