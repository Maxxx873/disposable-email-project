package com.disposableemail.job.config;

import com.disposableemail.config.MongoDbConfig;
import com.disposableemail.config.RabbitMqConfig;
import com.disposableemail.config.WebClientConfig;
import com.disposableemail.core.dao.mapper.search.MessageElasticsearchMapper;
import com.disposableemail.core.event.EventProducer;
import com.disposableemail.core.security.KeycloakClientConfig;
import com.disposableemail.core.security.TextEncryptorConfig;
import com.disposableemail.core.service.api.AccountService;
import com.disposableemail.core.service.api.MessageService;
import com.disposableemail.core.service.api.SourceService;
import com.disposableemail.core.service.api.auth.AuthorizationService;
import com.disposableemail.core.service.api.mail.MailServerClientService;
import com.disposableemail.core.service.api.search.MessageElasticsearchService;
import com.disposableemail.core.service.api.util.ElasticMongoIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Slf4j
@Configuration
@ComponentScan(
        basePackages =
                {
                        "com.disposableemail.core.service",
                        "com.disposableemail.core.event",
                        "com.disposableemail.core.dao.mapper",
                        "com.disposableemail.core.security",
                },
        useDefaultFilters = false,
        includeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes =
                {
                        AccountService.class,
                        SourceService.class,
                        MessageService.class,
                        EventProducer.class,
                        ElasticMongoIntegrationService.class,
                        MessageElasticsearchService.class,
                        AuthorizationService.class,
                        MailServerClientService.class,
                        MessageElasticsearchMapper.class,
                }))
@Import({
        WebClientConfig.class,
        TextEncryptorConfig.class,
        MongoDbConfig.class,
        RabbitMqConfig.class,
        KeycloakClientConfig.class,
})
@EnableReactiveMongoRepositories(basePackages = "com.disposableemail.core.dao.repository")
@EnableReactiveElasticsearchRepositories(basePackages = "com.disposableemail.core.dao.repository")
public class JobsAppConfig {
}

