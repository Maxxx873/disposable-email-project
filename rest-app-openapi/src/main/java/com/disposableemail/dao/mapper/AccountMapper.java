package com.disposableemail.dao.mapper;

import com.disposableemail.dao.entity.AccountEntity;
import com.disposableemail.rest.model.Account;
import com.disposableemail.service.api.mail.MailServerClientService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class AccountMapper {

    @Autowired
    protected MailServerClientService mailService;

    @Mapping(target = "quota", expression = "java(mailService.getQuotaSize(accountEntity.getAddress()).block())")
    @Mapping(target = "used", expression = "java(mailService.getUsedSize(accountEntity.getAddress()).block())")
    public abstract Account accountEntityToAccount(AccountEntity accountEntity);
    public abstract AccountEntity accountToAccountEntity(Account account);

}
