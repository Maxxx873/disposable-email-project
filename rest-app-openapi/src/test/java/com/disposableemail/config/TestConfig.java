package com.disposableemail.config;

import com.disposableemail.dao.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@TestConfiguration
@MockBeans({@MockBean(MessageRepository.class), @MockBean(ReactiveJwtDecoder.class)})
public class TestConfig {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ReactiveJwtDecoder reactiveJwtDecoder;
}
