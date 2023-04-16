package com.disposableemail.telegram.bot.model.mapper;

import com.disposableemail.telegram.bot.model.dto.MessageDto;
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
