package com.disposableemail.core.dao.mapper.search;

import com.disposableemail.core.dao.entity.MessageEntity;
import com.disposableemail.core.dao.entity.search.MessageElasticsearchEntity;
import com.disposableemail.core.model.Messages;
import org.mapstruct.*;

import java.util.Objects;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MessageElasticsearchMapper {

    @Mapping(source = "messageId", target = "id")
    @Mapping(source = "textBody", target = "text")
    @Mapping(source = "htmlBody", target = "html")
    @Mapping(source = "mimeMessageID", target = "msgid")
    @Mapping(target = "createdAt", expression = "java(messageElasticsearchEntity.getDate().toLocalDateTime())")
    @Mapping(target = "updatedAt", expression = "java(messageElasticsearchEntity.getDate().toLocalDateTime())")
    MessageEntity messageElasticsearchEntityToMessageEntity(MessageElasticsearchEntity messageElasticsearchEntity);

    @Mapping(source = "messageId", target = "id")
    @Mapping(source = "mimeMessageID", target = "msgid")
    @Mapping(target = "createdAt", expression = "java(messageElasticsearchEntity.getDate().toLocalDateTime())")
    @Mapping(target = "updatedAt", expression = "java(messageElasticsearchEntity.getDate().toLocalDateTime())")
    @Mapping(target = "downloadUrl",
            expression = "java(String.valueOf(\"/messages/\" + messageElasticsearchEntity.getMessageId() + \"/download\"))")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    Messages messageElasticsearchEntityToMessages(MessageElasticsearchEntity messageElasticsearchEntity);

    @AfterMapping
    default void updateResult(@MappingTarget Messages messages) {
        messages.getFrom().forEach(address -> {
            if (Objects.equals(address.getName(), null)) {
                address.setName("");
            }
        });
        messages.getTo().forEach(address -> {
            if (Objects.equals(address.getName(), null)) {
                address.setName("");
            }
        });
    }
}
