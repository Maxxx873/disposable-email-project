package com.disposableemail;

import com.disposableemail.config.TestConfig;
import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.dao.entity.DomainEntity;
import com.disposableemail.core.dao.entity.MessageEntity;
import com.disposableemail.core.dao.entity.SourceEntity;
import com.disposableemail.core.dao.mapper.DomainMapper;
import com.disposableemail.core.dao.mapper.MessageMapper;
import com.disposableemail.core.model.Account;
import com.disposableemail.core.model.Address;
import com.disposableemail.core.model.Source;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.api.DomainService;
import com.disposableemail.core.service.api.MessageService;
import com.disposableemail.core.service.api.SourceService;
import com.disposableemail.core.service.impl.AccountHelperService;
import com.disposableemail.facade.api.AccountFacade;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractSpringControllerIntegrationTest {

    @Autowired
    protected RateLimiterRegistry rateLimiterRegistry;

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected DomainMapper domainMapper;

    @Autowired
    protected MessageMapper messageMapper;

    @MockBean
    protected AccountService accountService;

    @MockBean
    protected AccountFacade accountFacade;

    @MockBean
    protected DomainService domainService;

    @MockBean
    protected AccountHelperService accountHelperService;

    @MockBean
    protected SourceService sourceService;

    @MockBean
    protected MessageService messageService;

    protected final AccountEntity testAccountEntity = AccountEntity.builder()
            .id("1")
            .address("account1@example.com")
            .quota(40000)
            .used(4000)
            .mailboxId("mailboxId1")
            .isDeleted(false)
            .isDisabled(true)
            .build();

    protected final List<DomainEntity> testDomainEntities = List.of(
            DomainEntity.builder()
                    .id("1")
                    .domain("example.com")
                    .isActive(true)
                    .isPrivate(false)
                    .build(),
            DomainEntity.builder()
                    .id("2")
                    .domain("example.org")
                    .isActive(false)
                    .isPrivate(true)
                    .build()
    );

    protected final Account testAccount = Account.builder()
            .id("1")
            .address("account1@example.com")
            .quota(40000)
            .used(4000)
            .isDeleted(false)
            .isDisabled(true)
            .build();

    protected final SourceEntity testSourceEntity = SourceEntity.builder()
            .id("1")
            .msgid("<1976018613.1.1685302342282@james.local>")
            .data("data")
            .build();

    protected final Source testSource = Source.builder()
            .id("1")
            .downloadUrl("/messages/1/download")
            .data("data")
            .build();

    protected final MessageEntity testMessageEntity = MessageEntity.builder()
            .id("1")
            .accountId("1")
            .msgid("msgid1")
            .from(List.of(Address.builder().name("sender1").address("sender1@example.com").build()))
            .to(List.of(Address.builder().name("recipient1").address("recipient1@example.com").build()))
            .cc(Collections.emptyList())
            .bcc(Collections.emptyList())
            .subject("subject")
            .isUnread(false)
            .isFlagged(false)
            .isDeleted(false)
            .text("text1")
            .hasAttachment(false)
            .attachments(Collections.emptyList())
            .size(600)
            .build();

}
