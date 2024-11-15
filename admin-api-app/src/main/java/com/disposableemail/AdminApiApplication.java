package com.disposableemail;

import com.disposableemail.core.event.handler.MessagesEventHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(exclude = {
        RedisAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class
})
@ComponentScan(excludeFilters =
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = MessagesEventHandler.class))
public class AdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiApplication.class, args);
    }
}
