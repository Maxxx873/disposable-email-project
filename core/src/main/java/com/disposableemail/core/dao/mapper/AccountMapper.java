package com.disposableemail.core.dao.mapper;

import com.disposableemail.core.dao.entity.AccountEntity;
import com.disposableemail.core.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    Account accountEntityToAccount(AccountEntity accountEntity);
    AccountEntity accountToAccountEntity(Account account);

}
