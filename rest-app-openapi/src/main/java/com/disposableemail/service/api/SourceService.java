package com.disposableemail.service.api;

import com.disposableemail.dao.entity.SourceEntity;
import reactor.core.publisher.Mono;

public interface SourceService {

    Mono<SourceEntity> getSourceByMsgId(String msgid);
}
