package com.disposableemail;

import com.disposableemail.config.TestConfig;
import com.disposableemail.core.dao.mapper.AccountMapper;
import com.disposableemail.core.service.api.AccountService;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractSpringControllerIntegrationTest {

    @Autowired
    protected RateLimiterRegistry rateLimiterRegistry;

    @Autowired
    protected WebTestClient webTestClient;

    @MockBean
    protected AccountService accountService;

    @Autowired
    protected AccountMapper accountMapper;

}
