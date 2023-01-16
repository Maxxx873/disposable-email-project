package com.disposableemail.config;

import com.disposableemail.rest.model.Credentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfiguration {

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        var jsonConverter = new Jackson2JsonMessageConverter(mapper);
        jsonConverter.setClassMapper(classMapper());
        return jsonConverter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        var classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("credentials", Credentials.class);
        classMapper.setTrustedPackages("*");
        classMapper.setIdClassMapping(idClassMapping);
        return classMapper;
    }
}
