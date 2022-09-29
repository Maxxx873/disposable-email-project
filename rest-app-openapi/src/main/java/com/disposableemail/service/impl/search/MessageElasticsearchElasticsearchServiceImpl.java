package com.disposableemail.service.impl.search;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.dao.entity.search.MessageElasticsearchEntity;
import com.disposableemail.dao.repository.search.MessageElasticsearchRepository;
import com.disposableemail.service.api.AccountService;
import com.disposableemail.service.api.MailServerClientService;
import com.disposableemail.service.api.search.MessageElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class MessageElasticsearchElasticsearchServiceImpl implements MessageElasticsearchService {

    private final MessageElasticsearchRepository messageElasticsearchRepository;
    private final AccountService accountService;

    private final MailServerClientService mailServerClientService;

    @Override
    public Flux<MessageElasticsearchEntity> getMessages(ServerWebExchange exchange) {
        log.info("Getting a Messages collection from Elasticsearch by recipient");

        return accountService.getAccountFromJwt(exchange).flatMapMany(accountEntity -> messageElasticsearchRepository
                .findByAddressTo(accountEntity.getAddress()).map(message -> {
                    message.setAccountId(accountEntity.getId());
                    return message;
                }));
    }

    @Override
    public Flux<MessageElasticsearchEntity> getMessagesFromMailbox(ServerWebExchange exchange) {
        log.info("Getting a Messages collection from Elasticsearch by mailboxId");

        return accountService.getAccountFromJwt(exchange).flatMapMany(accountEntity -> mailServerClientService
                .getMailboxId(accountEntity.getAddress(), "INBOX")
                .flatMapMany(messageElasticsearchRepository::findByMailboxId).map(message -> {
                    message.setAccountId(accountEntity.getId());
                    return message;
                }));
    }

    @Override
    public Mono<MessageElasticsearchEntity> getMessage(String id, ServerWebExchange exchange) {
        log.info("Getting a Message {} from Elasticsearch", id);

        return accountService.getAccountFromJwt(exchange)
                .map(AccountEntity::getAddress)
                .flatMap(address -> messageElasticsearchRepository.findByAddressToAndMessageId(address, id));
    }
}
