package com.disposableemail.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;

import reactor.kafka.receiver.ReceiverOptions;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    @ConditionalOnProperty(value = "spring.kafka.topic")
    public ReceiverOptions<String, String> receiverOptions(KafkaProperties props, @Value("${spring.kafka.topic}") String topic) {
        return ReceiverOptions.<String, String>create(props.buildConsumerProperties())
                .subscription(Collections.singletonList(topic));
    }

    @Bean
    @ConditionalOnProperty(value = "spring.kafka.topic")
    public ReactiveKafkaConsumerTemplate<String, String> consumerTemplate(ReceiverOptions<String, String> options) {
        return new ReactiveKafkaConsumerTemplate<>(options);
    }

}
