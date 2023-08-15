package com.disposableemail;

import com.disposableemail.config.TestConfig;
import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.model.Account;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.impl.AccountHelperService;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
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

    @MockBean
    protected AccountHelperService accountHelperService;

    protected final AccountEntity testAccountEntity = AccountEntity.builder()
            .id("1")
            .address("account1@example.com")
            .quota(40000)
            .used(4000)
            .mailboxId("mailboxId1")
            .isDeleted(false)
            .isDisabled(true)
            .build();

    protected final Account testAccount = Account.builder()
            .id("1")
            .address("account1@example.com")
            .quota(40000)
            .used(4000)
            .isDeleted(false)
            .isDisabled(true)
            .build();

}
