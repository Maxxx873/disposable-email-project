package com.disposableemail;

import com.disposableemail.config.TestConfig;
import com.disposableemail.core.dao.repository.AccountRepository;
import com.disposableemail.core.dao.repository.DomainRepository;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.api.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
public abstract class AbstractSpringIntegrationTest {

    @Autowired
    protected DomainService domainService;
    @Autowired
    protected DomainRepository domainRepository;
    @Autowired
    protected AccountService accountService;
    @Autowired
    protected AccountRepository accountRepository;
}
