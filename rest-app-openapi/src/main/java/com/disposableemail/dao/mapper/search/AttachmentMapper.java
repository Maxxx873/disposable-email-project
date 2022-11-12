package com.disposableemail.dao.mapper.search;

import com.disposableemail.dao.entity.search.AttachmentElasticsearchEntity;
import com.disposableemail.rest.model.Attachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttachmentMapper {

    @Mapping(source = "subtype", target = "contentType")
    @Mapping(source = "fileName", target = "filename")
    @Mapping(source = "contentDisposition", target = "disposition")
    Attachment attachmentElasticsearchEntityToAttachment(AttachmentElasticsearchEntity attachmentElasticsearchEntity);

}
