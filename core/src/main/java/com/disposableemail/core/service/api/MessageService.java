package com.disposableemail.core.service.api;

import com.disposableemail.core.dao.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides methods for managing messages in the system.
 */
public interface MessageService {

    /**
     * Retrieves a message by its ID.
     *
     * @param messageId the ID of the message to retrieve
     * @return a Mono that emits the message entity, or an error if the message does not exist
     */
    Mono<MessageEntity> getMessage(String messageId);

    /**
     * Saves a message to the system.
     *
     * @param messageEntity the message entity to save
     * @return a Mono that emits the saved message entity, or an error if the message cannot be saved
     */
    Mono<MessageEntity> saveMessage(MessageEntity messageEntity);

    /**
     * Retrieves a message by its ID, including soft-deleted messages.
     *
     * @param messageId the ID of the message to retrieve
     * @return a Mono that emits the message entity, or an error if the message does not exist
     */
    Mono<MessageEntity> getMessageById(String messageId);

    /**
     * Soft-deletes a message by its ID.
     *
     * @param messageId the ID of the message to soft-delete
     * @return a Mono that completes when the message has been soft-deleted, or an error if the message cannot be soft-deleted
     */
    Mono<MessageEntity> softDeleteMessage(String messageId);

    /**
     * Updates a message by its ID.
     *
     * @param messageId the ID of the message to update
     * @param messageEntity the updated message entity
     * @return a Mono that emits the updated message entity, or an error if the message cannot be updated
     */
    Mono<MessageEntity> updateMessage(String messageId, MessageEntity messageEntity);

    /**
     * Retrieves a page of messages for the authorized account.
     *
     * @param pageable the pageable object that specifies the page size and page number
     * @return a Flux that emits the page of messages for the authorized account
     */
    Flux<MessageEntity> getMessagesForAuthorizedAccount(Pageable pageable);

    /**
     * Deletes all messages older than a specified number of days.
     *
     * @param days the number of days to keep messages for
     * @return a Mono that completes when the messages have been deleted, or an error if the messages cannot be deleted
     */
    Mono<Void> deleteMessagesOlderNumberOfDays(Integer days);
}
