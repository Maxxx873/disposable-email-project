package com.disposableemail.dao.mapper;

import com.disposableemail.dao.entity.MessageEntity;
import com.disposableemail.dao.entity.search.MessageElasticsearchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MessageElasticsearchMapper {

    @Mapping(source = "messageId", target = "id")
    @Mapping(source = "textBody", target = "text")
    @Mapping(source = "htmlBody", target = "html")
    @Mapping(source = "mimeMessageID", target = "msgid")
    @Mapping(target = "createdAt", expression = "java(messageElasticsearchEntity.getDate().toLocalDateTime())")
    MessageEntity messageElasticsearchEntityToMessageEntity(MessageElasticsearchEntity messageElasticsearchEntity);

    MessageElasticsearchEntity messageEntityToMessageElasticsearchEntity(MessageEntity messageEntity);

    default Mono<MessageElasticsearchEntity> messageEntityToMessageElasticsearchEntity(Mono<MessageEntity> mono) {
        return mono.map(this::messageEntityToMessageElasticsearchEntity);
    }

}
