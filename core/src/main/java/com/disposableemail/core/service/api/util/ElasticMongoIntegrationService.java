package com.disposableemail.core.service.api.util;

import com.disposableemail.core.dao.entity.AccountEntity;

public interface ElasticMongoIntegrationService {
    void saveMessagesFromElasticsearchMailboxToMongo(AccountEntity accountEntity);

}
