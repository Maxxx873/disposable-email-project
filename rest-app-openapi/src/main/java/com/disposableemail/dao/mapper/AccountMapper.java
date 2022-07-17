package com.disposableemail.dao.mapper;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.rest.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    Account accountEntityToAccount(AccountEntity accountEntity);

    AccountEntity accountToAccountEntity(Account account);

    default Mono<AccountEntity> accountToAccountEntity(Mono<Account> mono) {
        return mono.map(this::accountToAccountEntity);
    }
}
