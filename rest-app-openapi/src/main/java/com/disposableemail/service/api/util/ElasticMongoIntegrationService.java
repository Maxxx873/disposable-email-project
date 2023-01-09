package com.disposableemail.service.api.util;

import com.disposableemail.dao.entity.AccountEntity;

public interface ElasticMongoIntegrationService {
    void saveMessagesFromElasticsearchMailboxToMongo(AccountEntity accountEntity);

}
