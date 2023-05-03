package com.disposableemail.core.dao.mapper;

import com.disposableemail.core.dao.entity.MessageEntity;
import com.disposableemail.core.model.Message;
import com.disposableemail.core.model.Messages;
import org.mapstruct.*;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface MessageMapper {

    @Mapping(target = "downloadUrl",
            expression = "java(String.valueOf(\"/messages/\" + messageEntity.getId() + \"/download\"))")
    @Mapping(target = "text", defaultValue = "")
    @Mapping(target = "html", defaultExpression = "java(new ArrayList<>())")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    Message messageEntityToMessage(MessageEntity messageEntity);

    MessageEntity messageToMessageEntity(Message message);

    default Mono<MessageEntity> messageToMessageEntity(Mono<Message> mono) {
        return mono.map(this::messageToMessageEntity);
    }

    @Mapping(target = "downloadUrl",
            expression = "java(String.valueOf(\"/messages/\" + messageEntity.getId() + \"/download\"))")
    Messages messageEntityToMessages(MessageEntity messageEntity);

    MessageEntity messagesToMessageEntity(Messages messages);

    default Mono<MessageEntity> messagesToMessageEntity(Mono<Messages> mono) {
        return mono.map(this::messagesToMessageEntity);
    }

    @AfterMapping
    default void updateResult(@MappingTarget Message message) {
        if (Objects.nonNull(message.getFrom())) {
            message.getFrom().forEach(address -> {
                if (Objects.equals(address.getName(), null)) {
                    address.setName("");
                }
            });
        }
        if (Objects.nonNull(message.getTo())) {
            message.getTo().forEach(address -> {
                if (Objects.equals(address.getName(), null)) {
                    address.setName("");
                }
            });
        }
        if (Objects.nonNull(message.getBcc())) {
            message.getBcc().forEach(address -> {
                if (Objects.equals(address.getName(), null)) {
                    address.setName("");
                }
            });
        }
        message.getAttachments().forEach(attachment ->
                attachment.setDownloadUrl(String.format("/messages/%s/attachment/%s", message.getId(), attachment.getId())));
    }
}