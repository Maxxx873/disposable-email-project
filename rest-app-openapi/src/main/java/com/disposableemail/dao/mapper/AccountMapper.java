package com.disposableemail.dao.mapper;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.rest.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    public abstract Account accountEntityToAccount(AccountEntity accountEntity);
    public abstract AccountEntity accountToAccountEntity(Account account);

}
