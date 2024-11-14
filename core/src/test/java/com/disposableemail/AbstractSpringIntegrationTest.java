package com.disposableemail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.disposableemail.config.TestConfig;
import com.disposableemail.core.dao.repository.AccountRepository;
import com.disposableemail.core.dao.repository.DomainRepository;
import com.disposableemail.core.dao.repository.MessageRepository;
import com.disposableemail.core.dao.repository.SourceRepository;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.api.DomainService;
import com.disposableemail.core.service.api.MessageService;
import com.disposableemail.core.service.api.SourceService;
import com.disposableemail.core.service.api.auth.AuthorizationService;
import com.disposableemail.core.service.api.mail.MailServerClientService;
import com.disposableemail.core.service.impl.AccountHelperService;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
        RedisAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class
})
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
public abstract class AbstractSpringIntegrationTest {

    @Value("${mail-server.mailbox}")
    protected String inbox;
    @Autowired
    protected DomainService domainService;
    @Autowired
    protected DomainRepository domainRepository;
    @Autowired
    protected AccountService accountService;
    @Autowired
    protected AccountRepository accountRepository;
    @Autowired
    protected SourceRepository sourceRepository;
    @Autowired
    protected SourceService sourceService;
    @Autowired
    protected MessageService messageService;
    @Autowired
    protected MessageRepository messageRepository;
    @Autowired
    protected AccountHelperService accountHelperService;
    @MockBean
    protected AuthorizationService authorizationService;
    @MockBean
    protected MailServerClientService mailServerClientService;
}
