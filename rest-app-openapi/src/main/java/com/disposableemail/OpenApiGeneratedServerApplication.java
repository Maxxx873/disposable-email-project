package com.disposableemail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableReactiveMongoAuditing
public class OpenApiGeneratedServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenApiGeneratedServerApplication.class, args);
    }
}
