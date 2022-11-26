package com.disposableemail.dao.mapper;

import com.disposableemail.dao.entity.AttachmentEntity;
import com.disposableemail.rest.model.Attachment;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttachmentMapper {

    Attachment attachmentEntityToAttachment(AttachmentEntity attachmentEntity);

    AttachmentEntity attachmentToAttachmentEntity(Attachment attachment);

    default Mono<AttachmentEntity> attachmentToAttachmentEntity(Mono<Attachment> mono) {
        return mono.map(this::attachmentToAttachmentEntity);
    }

}
