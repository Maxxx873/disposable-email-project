package com.disposableemail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class OpenApiGeneratedServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenApiGeneratedServerApplication.class, args);
    }
}
