package com.disposableemail.dao.mapper;

import com.disposableemail.dao.entity.SourceEntity;
import com.disposableemail.rest.model.Source;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SourceMapper {

    Source sourceEntityToSource(SourceEntity sourceEntity);

    SourceEntity sourceToSourceEntity(Source source);

    default Mono<SourceEntity> sourceToSourceEntity(Mono<Source> mono) {
        return mono.map(this::sourceToSourceEntity);
    }
}
