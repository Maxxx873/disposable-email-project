package com.disposableemail.service.impl;

import com.disposableemail.dao.entity.SourceEntity;
import com.disposableemail.dao.repository.SourceRepository;
import com.disposableemail.service.api.SourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceServiceImpl implements SourceService {

    private final SourceRepository sourceRepository;

    @Override
    public Mono<SourceEntity> getSourceByMsgId(String msgid) {
        log.info("Getting a Source by msgid {}", msgid);

        return sourceRepository.findByMsgid(msgid);
    }
}
