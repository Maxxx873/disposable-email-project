package com.disposableemail.core.dao.mapper;

import com.disposableemail.core.dao.entity.SourceEntity;
import com.disposableemail.core.model.Source;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SourceMapper {

    @Mapping(target = "downloadUrl",
            expression = "java(String.valueOf(\"/messages/\" + sourceEntity.getId() + \"/download\"))")
    Source sourceEntityToSource(SourceEntity sourceEntity);

    SourceEntity sourceToSourceEntity(Source source);

    default Mono<SourceEntity> sourceToSourceEntity(Mono<Source> mono) {
        return mono.map(this::sourceToSourceEntity);
    }
}
