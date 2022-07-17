package com.disposableemail.dao.mapper;

import com.disposableemail.rest.model.Account;
import com.disposableemail.rest.model.Credentials;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CredentialsMapper {

    default Mono<Account> credentialsToAccountMono(Mono<Credentials> credentialsMono) {
        return credentialsMono.map(this::credentialsToAccount);
    }

    Account credentialsToAccount(Credentials credentials);

}
