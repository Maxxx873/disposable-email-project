package com.disposableemail.dao.mapper;

import com.disposableemail.dao.entity.MessageEntity;
import com.disposableemail.rest.model.Message;
import com.disposableemail.rest.model.Messages;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface MessageMapper {

    @Mapping(target = "downloadUrl",
            expression = "java(String.valueOf(\"/messages/\" + messageEntity.getId() + \"/download\"))")
    @Mapping(target = "text", defaultValue = "")
    @Mapping(target = "html", defaultExpression = "java(new ArrayList<>())")
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

}