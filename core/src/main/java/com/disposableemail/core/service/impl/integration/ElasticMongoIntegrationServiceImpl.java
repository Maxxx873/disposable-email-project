package com.disposableemail.core.service.impl.integration;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.dao.mapper.search.MessageElasticsearchMapper;
import com.disposableemail.core.service.api.MessageService;
import com.disposableemail.core.service.api.SourceService;
import com.disposableemail.core.service.api.search.MessageElasticsearchService;
import com.disposableemail.core.service.api.util.ElasticMongoIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticMongoIntegrationServiceImpl implements ElasticMongoIntegrationService {

    private final SourceService sourceService;
    private final MessageElasticsearchService messageElasticsearchService;
    private final MessageElasticsearchMapper messageElasticsearchMapper;
    private final MessageService messageService;

    @Override
    public void saveMessagesFromElasticsearchMailboxToMongo(AccountEntity accountEntity) {
        log.info("Saving a Message collection from Elasticsearch to Mongo | Account: {}, Mailbox: {}",
                accountEntity.getAddress(), accountEntity.getMailboxId());

        var result = messageElasticsearchService.getMessagesFromMailbox(accountEntity)
                .flatMap(messageElasticsearchEntity ->
                        messageService.getMessageById(messageElasticsearchEntity.getMessageId())
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.info("Added a new Message | Id: {} ", messageElasticsearchEntity.getMessageId());
                                    var message = messageElasticsearchMapper
                                            .messageElasticsearchEntityToMessageEntity(messageElasticsearchEntity);

                                    return sourceService.getAttachments(message.getMsgid())
                                            .map(attachmentEntities -> {
                                                attachmentEntities.forEach(attachmentEntity ->
                                                        attachmentEntity.setDownloadUrl("/messages/" + message.getId() +
                                                                "/attachment/" + attachmentEntity.getId()));
                                                return attachmentEntities;
                                            })
                                            .flatMap(attachmentEntities -> {
                                                message.setAttachments(attachmentEntities);
                                                return messageService.saveMessage(message);
                                            });
                                }))
                                .or(Mono.defer(() -> {
                                    log.info("Updated an existing Message | Id: {} ", messageElasticsearchEntity.getMessageId());
                                    var message = messageElasticsearchMapper
                                            .messageElasticsearchEntityToMessageEntity(messageElasticsearchEntity);
                                    return messageService.saveMessage(message);
                                })));
        result.subscribe();
    }
}
