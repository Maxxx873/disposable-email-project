package com.disposableemail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@SpringBootApplication
@EnableReactiveMongoAuditing
public class OpenApiGeneratedServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenApiGeneratedServerApplication.class, args);
    }

}
