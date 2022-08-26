package com.disposableemail.dao.mapper;

import com.disposableemail.dao.entity.MessageEntity;
import com.disposableemail.rest.model.Message;
import com.disposableemail.rest.model.Messages;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MessageMapper {

    @Mapping(source = "messageId", target = "id")
    @Mapping(source = "textBody", target = "text")
    @Mapping(source = "htmlBody", target = "html")
    @Mapping(source = "mimeMessageID", target = "msgid")
    @Mapping(target = "createdAt", expression = "java(messageEntity.getDate().toLocalDateTime())")
    Message messageEntityToMessage(MessageEntity messageEntity);

    MessageEntity messageToMessageEntity(Message message);

    default Mono<MessageEntity> messageToMessageEntity(Mono<Message> mono) {
        return mono.map(this::messageToMessageEntity);
    }

    @Mapping(source = "messageId", target = "id")
    @Mapping(source = "mimeMessageID", target = "msgid")
    @Mapping(target = "createdAt", expression = "java(messageEntity.getDate().toLocalDateTime())")
    Messages messageEntityToMessages(MessageEntity messageEntity);

    MessageEntity messagesToMessageEntity(Messages messages);

    default Mono<MessageEntity> messagesToMessageEntity(Mono<Messages> mono) {
        return mono.map(this::messagesToMessageEntity);
    }
}
