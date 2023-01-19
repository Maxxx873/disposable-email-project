package com.disposableemail.service.impl.util;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.dao.mapper.search.MessageElasticsearchMapper;
import com.disposableemail.service.api.MessageService;
import com.disposableemail.service.api.SourceService;
import com.disposableemail.service.api.search.MessageElasticsearchService;
import com.disposableemail.service.api.util.ElasticMongoIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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

    @Async
    @Override
/*    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "${exchanges.messages}", type = ExchangeTypes.TOPIC),
            value = @Queue(name = "${queues.messages-getting}"),
            key = "${routing-keys.messages-getting}"
    ))*/
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
                                })));
        result.subscribe();
    }
}
