package com.disposableemail.telegram.dto;

import com.disposableemail.telegram.client.disposableemail.webclient.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MessageMapper {

    @Mapping(source = "sentDate", target = "date")
    MessageDto messageToDto(Message message);
}
