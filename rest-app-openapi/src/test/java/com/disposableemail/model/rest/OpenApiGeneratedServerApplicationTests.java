package com.disposableemail.model.rest;

import com.disposableemail.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
class OpenApiGeneratedServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
