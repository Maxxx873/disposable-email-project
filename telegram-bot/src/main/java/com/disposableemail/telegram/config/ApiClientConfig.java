package com.disposableemail.telegram.config;

import com.disposableemail.telegram.client.disposableemail.webclient.ApiClient;
import com.disposableemail.telegram.client.disposableemail.webclient.api.DefaultApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class ApiClientConfig {

    private final WebClient webclient;
    private final String disposableEmailApiBasePath;

    public ApiClientConfig(
            WebClient webclient,
            @Value("${disposableemail.api.base-path}") String disposableEmailApiBasePath
    ) {
        this.webclient = webclient;
        this.disposableEmailApiBasePath = disposableEmailApiBasePath;
    }

    @Bean
    public DefaultApi disposableEmailApiClient() {
        return new DefaultApi(
                new ApiClient(webclient).setBasePath(disposableEmailApiBasePath)
        );
    }

}
