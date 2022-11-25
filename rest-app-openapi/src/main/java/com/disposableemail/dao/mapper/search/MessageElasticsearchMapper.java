package com.disposableemail.dao.mapper.search;

import com.disposableemail.dao.entity.MessageEntity;
import com.disposableemail.dao.entity.search.MessageElasticsearchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = AttachmentMapper.class)
public interface MessageElasticsearchMapper {

    @Mapping(source = "messageId", target = "id")
    @Mapping(source = "textBody", target = "text")
    @Mapping(source = "htmlBody", target = "html")
    @Mapping(source = "mimeMessageID", target = "msgid")
    @Mapping(target = "createdAt", expression = "java(messageElasticsearchEntity.getDate().toLocalDateTime())")
    @Mapping(target = "updatedAt", expression = "java(messageElasticsearchEntity.getDate().toLocalDateTime())")
    MessageEntity messageElasticsearchEntityToMessageEntity(MessageElasticsearchEntity messageElasticsearchEntity);

}