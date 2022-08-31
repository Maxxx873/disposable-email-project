package com.disposableemail.config;

import com.disposableemail.dao.repository.MessageElasticsearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@TestConfiguration
@MockBeans({@MockBean(MessageElasticsearchRepository.class), @MockBean(ReactiveJwtDecoder.class)})
public class TestConfig {
    @Autowired
    private MessageElasticsearchRepository messageElasticsearchRepository;
    @Autowired
    private ReactiveJwtDecoder reactiveJwtDecoder;
}
