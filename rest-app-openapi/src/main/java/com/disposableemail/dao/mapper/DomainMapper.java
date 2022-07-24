package com.disposableemail.dao.mapper;

import com.disposableemail.dao.entity.DomainEntity;
import com.disposableemail.rest.model.Domain;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DomainMapper {

    Domain domainEntityToDomain(DomainEntity domainEntity);

    DomainEntity domainToDomainEntity(Domain domain);

    default Mono<DomainEntity> domainToDomainEntity(Mono<Domain> mono) {
        return mono.map(this::domainToDomainEntity);
    }
}
